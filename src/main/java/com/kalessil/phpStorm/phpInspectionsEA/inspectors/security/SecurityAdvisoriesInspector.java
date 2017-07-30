package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;

import java.util.Collection;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static final Collection<String> developmentPackages = new HashSet<>();
    static {
        /* PhpUnit */
        developmentPackages.add("phpunit/phpunit");
        developmentPackages.add("phpunit/dbunit");
        developmentPackages.add("johnkary/phpunit-speedtrap");
        developmentPackages.add("symfony/phpunit-bridge");

        /* more dev-packages  */
        developmentPackages.add("mockery/mockery");
        developmentPackages.add("behat/behat");
        developmentPackages.add("phpspec/prophecy");
        developmentPackages.add("phpspec/phpspec");
        developmentPackages.add("composer/composer");
        developmentPackages.add("satooshi/php-coveralls");
        developmentPackages.add("phpro/grumphp");

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
        developmentPackages.add("povils/phpmnd");

        /* build tools */
        developmentPackages.add("phing/phing");
    }

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        /* verify file name and it's validity */
        if (!"composer.json".equals(file.getName()) || !(file.getFirstChild() instanceof JsonObject)) {
            return null;
        }

        final JsonObject config = (JsonObject) file.getFirstChild();

        /* find "require" and "name" sections */
        final JsonProperty requireProperty  = config.findProperty("require");
        String             ownPackageName   = null;
        String             ownPackagePrefix = null;

        for (final PsiElement option : config.getChildren()) {
            if (option instanceof JsonProperty) {
                final String propertyName = ((PsiNamedElement) option).getName();

                if ("name".equals(propertyName)) {
                    final JsonValue nameValue = ((JsonProperty) option).getValue();

                    if (nameValue instanceof JsonStringLiteral) {
                        ownPackageName = ((JsonStringLiteral) nameValue).getValue();

                        if (ownPackageName.indexOf('/') != -1) {
                            ownPackagePrefix = ownPackageName.substring(0, 1 + ownPackageName.indexOf('/'));
                        }
                    }
                }
            }
        }

        /* inspect packages, they should be by other owner */
        final ProblemsHolder holder               = new ProblemsHolder(manager, file, isOnTheFly);
        final JsonValue      requiredPackagesList = (requireProperty == null) ? null : requireProperty.getValue();

        if ((requiredPackagesList instanceof JsonObject) && !developmentPackages.contains(ownPackageName)) {
            boolean hasAdvisories           = false;
            int     thirdPartyPackagesCount = 0;

            for (final PsiElement component : requiredPackagesList.getChildren()) {
                /* we expect certain structure for package definition */
                if (!(component instanceof JsonProperty)) {
                    continue;
                }

                /* if advisories already there, verify usage of dev-master */
                final JsonProperty dependency     = (JsonProperty) component;
                final String       packageName    = dependency.getName().toLowerCase();
                final JsonValue    packageVersion = dependency.getValue();

                if ((packageVersion instanceof JsonStringLiteral) && "roave/security-advisories".equals(packageName)) {
                    if (!"\"dev-master\"".equals(packageVersion.getText().toLowerCase())) {
                        holder.registerProblem(packageVersion, useMaster);
                    }

                    hasAdvisories = true;
                }

                if (developmentPackages.contains(packageName)) {
                    holder.registerProblem(dependency.getFirstChild(), useRequireDev);
                }
                else if (packageName.indexOf('/') != -1) {
                    if ((ownPackagePrefix == null) || !packageName.startsWith(ownPackagePrefix)) {
                        ++thirdPartyPackagesCount;
                    }
                }
            }

            /* fire error message if we have any of 3rd-party packages */
            if ((thirdPartyPackagesCount > 0) && !hasAdvisories) {
                holder.registerProblem(requireProperty.getFirstChild(), message);
            }
        }

        return holder.getResultsArray();
    }
}
