package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.*;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SecurityAdvisoriesInspector extends LocalInspectionTool {
    private static final String message       = "Please add roave/security-advisories:dev-master as a firewall for vulnerable components.";
    private static final String useMaster     = "Please use dev-master instead.";
    private static final String useRequireDev = "Dev-packages have no security guaranties, invoke the package via require-dev instead.";

    private static final Set<String> developmentPackages = new HashSet<>();
    static {
        /* PhpUnit */
        developmentPackages.add("phpunit/phpunit");
        developmentPackages.add("phpunit/dbunit");
        developmentPackages.add("johnkary/phpunit-speedtrap");
        developmentPackages.add("symfony/phpunit-bridge");
        /* more dev-packages  */
        developmentPackages.add("mockery/mockery");
        developmentPackages.add("behat/behat");
        developmentPackages.add("phpspec/phpspec");
        developmentPackages.add("composer/composer");
        developmentPackages.add("satooshi/php-coveralls");
        /* SCA tools */
        developmentPackages.add("friendsofphp/php-cs-fixer");
        developmentPackages.add("squizlabs/php_codesniffer");
        developmentPackages.add("phpstan/phpstan");
        developmentPackages.add("vimeo/psalm");
        developmentPackages.add("jakub-onderka/php-parallel-lint");
        developmentPackages.add("slevomat/coding-standard");
        developmentPackages.add("phpmd/phpmd");
        developmentPackages.add("pdepend/pdepend");
        developmentPackages.add("sebastian/phpcpd");
    }

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        /* verify file name and it's validity */
        final PsiElement config = file.getFirstChild();
        if (!file.getName().equals("composer.json") || !(config instanceof JsonObject)) {
            return null;
        }

        /* find "require" and "name" sections */
        JsonProperty requireProperty = null;
        String ownPackageName        = null;
        String ownPackagePrefix      = null;
        for (final PsiElement option : config.getChildren()) {
            if (option instanceof JsonProperty) {
                final String propertyName = ((JsonProperty) option).getName();
                if (!StringUtil.isEmpty(propertyName)) {
                    if (propertyName.equals("name")) {
                        final JsonValue nameValue = ((JsonProperty) option).getValue();
                        if (nameValue instanceof JsonStringLiteral) {
                            ownPackageName = ownPackagePrefix = ((JsonStringLiteral) nameValue).getValue();
                            ownPackagePrefix = 0 == ownPackagePrefix.length() ? null : ownPackagePrefix;
                            if (null != ownPackagePrefix && -1 != ownPackagePrefix.indexOf('/')) {
                                ownPackagePrefix = ownPackagePrefix.substring(0, 1 + ownPackagePrefix.indexOf('/'));
                            }
                        }

                        continue;
                    }

                    if (propertyName.equals("require")) {
                        requireProperty = (JsonProperty) option;
                        break;
                    }
                }
            }
        }


        /* inspect packages, they should be by other owner */
        final ProblemsHolder holder          = new ProblemsHolder(manager, file, isOnTheFly);
        final JsonValue requiredPackagesList = requireProperty == null ? null : requireProperty.getValue();
        if (requiredPackagesList instanceof JsonObject && !developmentPackages.contains(ownPackageName)) {
            int thirdPartyPackagesCount = 0;
            for (final PsiElement component : requiredPackagesList.getChildren()) {
                /* we expect certain structure for package definition */
                if (!(component instanceof JsonProperty)) {
                    continue;
                }

                /* if advisories already there, verify usage of dev-master */
                final JsonProperty dependency  = (JsonProperty) component;
                final String packageName       = dependency.getName().toLowerCase();
                final JsonValue packageVersion = dependency.getValue();

                if (packageName.equals("roave/security-advisories") && packageVersion instanceof JsonStringLiteral) {
                    if (!packageVersion.getText().toLowerCase().equals("\"dev-master\"")) {
                        holder.registerProblem(packageVersion, useMaster, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                    continue;
                }

                if (developmentPackages.contains(packageName)) {
                    holder.registerProblem(dependency.getFirstChild(), useRequireDev, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                } else if (packageName.indexOf('/') != -1) {
                    if (ownPackagePrefix == null || !packageName.startsWith(ownPackagePrefix)) {
                        ++thirdPartyPackagesCount;
                    }
                }
            }

            /* fire error message if we have any of 3rd-party packages */
            if (thirdPartyPackagesCount > 0) {
                holder.registerProblem(requireProperty.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }

        return holder.getResultsArray();
    }
}
