package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.*;
import com.intellij.json.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

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

    // Inspection options.
    public final List<String> optionConfiguration = new ArrayList<>();
    public boolean optionConfigurationMigrated;

    public static Collection<String> optionConfigurationDefaults() {
        final Collection<String> developmentPackages = new TreeSet<>();

        /* PhpUnit */
        developmentPackages.add("phpunit/phpunit");
        developmentPackages.add("phpunit/dbunit");
        developmentPackages.add("johnkary/phpunit-speedtrap");

        /* frameworks */
        developmentPackages.add("symfony/phpunit-bridge");
        developmentPackages.add("symfony/dotenv");
        developmentPackages.add("symfony/debug");
        developmentPackages.add("symfony/var-dumper");

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

        return developmentPackages;
    }

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(
            (component) -> component.addList("Development packages:",
                                             optionConfiguration, SecurityAdvisoriesInspector::optionConfigurationDefaults,
                                             null,
                                             "Adding custom development package...", "Examples: \"phpunit/phpunit\"")
        );
    }

    @SuppressWarnings ("ThrowsRuntimeException")
    @Override
    public void readSettings(@NotNull final Element node) throws InvalidDataException {
        super.readSettings(node);

        if (!optionConfigurationMigrated) {
            optionConfiguration.clear();
            optionConfiguration.addAll(optionConfigurationDefaults());

            optionConfigurationMigrated = true;
        }
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        /* verify file name and its validity */
        if (!"composer.json".equals(file.getName()) || !(file.getFirstChild() instanceof JsonObject)) {
            return null;
        }

        final JsonObject config = (JsonObject) file.getFirstChild();

        /* find "require" section, it is required here */
        final JsonProperty requireProperty = config.findProperty("require");
        if (requireProperty == null) {
            return null;
        }
        final JsonValue requiredPackagesList = requireProperty.getValue();
        if (!(requiredPackagesList instanceof JsonObject)) {
            return null;
        }

        /* if library type is implicitly defined: if so, do not hit as we can break minimum stability requirements */
        final JsonProperty typeProperty = config.findProperty("type");
        if (typeProperty != null) {
            final JsonValue type = typeProperty.getValue();
            if (type instanceof JsonStringLiteral && "library".equals(((JsonStringLiteral) type).getValue())) {
                return null;
            }
        }

        final JsonProperty nameProperty     = config.findProperty("name");
        String             ownPackagePrefix = null;

        if (nameProperty != null) {
            final JsonValue namePropertyValue = nameProperty.getValue();

            if (namePropertyValue instanceof JsonStringLiteral) {
                final String ownPackageName = ((JsonStringLiteral) namePropertyValue).getValue();

                if (optionConfiguration.contains(ownPackageName)) {
                    return null;
                }

                if (ownPackageName.indexOf('/') != -1) {
                    ownPackagePrefix = ownPackageName.substring(0, ownPackageName.indexOf('/') + 1);
                }
            }
        }

        /* inspect packages, they should be by other owner */
        final ProblemsHolder holder                = new ProblemsHolder(manager, file, isOnTheFly);
        boolean              hasAdvisories         = false;
        boolean              hasThirdPartyPackages = false;

        for (final JsonProperty component : ((JsonObject) requiredPackagesList).getPropertyList()) {
            final JsonValue packageVersion = component.getValue();
            if (!(packageVersion instanceof JsonStringLiteral)) {
                continue;
            }

            /* if advisories already there, verify usage of dev-master */
            final String packageName = component.getName().toLowerCase();
            if ("roave/security-advisories".equals(packageName)) {
                if (!"dev-master".equals(((JsonStringLiteral) packageVersion).getValue().toLowerCase())) {
                    holder.registerProblem(packageVersion, useMaster);
                }

                hasAdvisories = true;
            }

            if (optionConfiguration.contains(packageName)) {
                holder.registerProblem(component.getFirstChild(), useRequireDev);
            }

            if (packageName.indexOf('/') != -1) {
                if (ownPackagePrefix == null || !packageName.startsWith(ownPackagePrefix)) {
                    hasThirdPartyPackages = true;
                }
            }
        }

        /* fire error message if we have any of 3rd-party packages */
        if (hasThirdPartyPackages && !hasAdvisories) {
            holder.registerProblem(requireProperty.getFirstChild(), message, new AddAdvisoriesFix(requireProperty));
        }

        return holder.getResultsArray();
    }

    private static class AddAdvisoriesFix implements LocalQuickFix {
        private final SmartPsiElementPointer<JsonProperty> require;

        AddAdvisoriesFix(@NotNull JsonProperty require) {
            final SmartPointerManager factory = SmartPointerManager.getInstance(require.getProject());
            this.require                      = factory.createSmartPsiElementPointer(require);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "Require 'roave/security-advisories' package";
        }

        @NotNull
        @Override
        public String getName() {
            return getFamilyName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            final JsonProperty require = this.require.getElement();
            if (require != null && !project.isDisposed()) {
                final PsiElement packages = require.getValue();
                if (packages instanceof JsonObject) {
                    final PsiElement marker = packages.getFirstChild();
                    if (marker != null) {
                        final LeafPsiElement comma = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                        if (comma != null) {
                            final String code           = "\"roave/security-advisories\": \"dev-master\"";
                            final PsiElement advisories = new JsonElementGenerator(project).createObject(code).getPropertyList().get(0);
                            marker.getParent().addAfter(comma, marker);
                            marker.getParent().addAfter(advisories, marker);
                        }
                    }
                }
            }
        }
    }
}
