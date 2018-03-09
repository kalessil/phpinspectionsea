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
    public boolean REPORT_MISSING_ROAVE_ADVISORIES = true;
    public boolean REPORT_MISPLACED_DEPENDENCIES   = true;
    public final List<String> optionConfiguration  = new ArrayList<>();
    public boolean optionConfigurationMigrated;

    public static Collection<String> optionConfigurationDefaults() {
        final Collection<String> developmentPackages = new TreeSet<>();

        /* Not included: webmozart/assert, beberlei/assert - can be used in production */

        /* PhpUnit */
        developmentPackages.add("phpunit/phpunit");
        developmentPackages.add("phpunit/dbunit");
        developmentPackages.add("johnkary/phpunit-speedtrap");
        developmentPackages.add("brianium/paratest");
        developmentPackages.add("phpunit/phpcov");
        developmentPackages.add("mybuilder/phpunit-accelerator");
        developmentPackages.add("phpunit/phpunit-selenium");

        /* frameworks: Symfony, ZF2, Yii2 */
        developmentPackages.add("symfony/phpunit-bridge");
        developmentPackages.add("symfony/debug");
        developmentPackages.add("symfony/var-dumper");
        developmentPackages.add("zendframework/zend-test");
        developmentPackages.add("zendframework/zend-debug");
        developmentPackages.add("yiisoft/yii2-gii");
        developmentPackages.add("yiisoft/yii2-debug");

        /* more dev-packages  */
        developmentPackages.add("codeception/codeception");
        developmentPackages.add("behat/behat");
        developmentPackages.add("phpspec/prophecy");
        developmentPackages.add("phpspec/phpspec");
        developmentPackages.add("humbug/humbug");
        developmentPackages.add("infection/infection");
        developmentPackages.add("mockery/mockery");
        developmentPackages.add("satooshi/php-coveralls");
        developmentPackages.add("mikey179/vfsStream");
        developmentPackages.add("filp/whoops");

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
        developmentPackages.add("phan/phan");
        developmentPackages.add("phpro/grumphp");
        developmentPackages.add("wimg/php-compatibility");
        developmentPackages.add("sstalle/php7cc");

        /* build and package management tools */
        developmentPackages.add("phing/phing");
        developmentPackages.add("composer/composer");
        developmentPackages.add("roave/security-advisories");

        return developmentPackages;
    }

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report missing 'roave/security-advisories'", REPORT_MISSING_ROAVE_ADVISORIES, (isSelected) -> REPORT_MISSING_ROAVE_ADVISORIES = isSelected);
            component.addCheckbox("Report dev-packages in require-section", REPORT_MISPLACED_DEPENDENCIES, (isSelected) -> REPORT_MISPLACED_DEPENDENCIES = isSelected);

            component.addList(
                "Development packages:",
                optionConfiguration,
                SecurityAdvisoriesInspector::optionConfigurationDefaults,
                null,
                "Adding custom development package...",
                "Examples: 'phpunit/phpunit'"
            );
        });
    }

    @Override
    public void readSettings(@NotNull final Element node) throws InvalidDataException {
        super.readSettings(node);

        if (!optionConfigurationMigrated || optionConfiguration.isEmpty()) {
            optionConfiguration.addAll(optionConfigurationDefaults());
            optionConfigurationMigrated = true;
        }
    }

    private boolean isLibrary(@NotNull JsonObject manifest) {
        boolean result              = false;
        final JsonProperty property = manifest.findProperty("type");
        if (property != null) {
            final JsonValue value = property.getValue();
            result = value instanceof JsonStringLiteral && ((JsonStringLiteral) value).getValue().equals("library");
        }
        return result;
    }

    @Nullable
    private String getVendorName(@NotNull JsonObject manifest) {
        String result               = null;
        final JsonProperty property = manifest.findProperty("name");
        if (property != null) {
            final JsonValue value = property.getValue();
            if (value instanceof JsonStringLiteral) {
                final String packageName = ((JsonStringLiteral) value).getValue();
                if (optionConfiguration.contains(packageName)) {
                    result = packageName;
                } else if (packageName.indexOf('/') != -1) {
                    result = packageName.substring(0, packageName.indexOf('/') + 1);
                }
            }
        }
        return result;
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

        /* skip analyzing libraries (we can break minimum stability requirements) */
        if (this.isLibrary(config)) {
            return null;
        }

        /* identify package owner; skip analyzing dev-packages */
        final String ownPackagePrefix = this.getVendorName(config);
        if (ownPackagePrefix != null && optionConfiguration.contains(ownPackagePrefix)) {
            return null;
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

            if (REPORT_MISPLACED_DEPENDENCIES && optionConfiguration.contains(packageName)) {
                holder.registerProblem(component.getFirstChild(), useRequireDev);
            }

            if (packageName.indexOf('/') != -1) {
                if (ownPackagePrefix == null || !packageName.startsWith(ownPackagePrefix)) {
                    hasThirdPartyPackages = true;
                }
            }
        }

        /* fire error message if we have any of 3rd-party packages */
        if (REPORT_MISSING_ROAVE_ADVISORIES && hasThirdPartyPackages && !hasAdvisories) {
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
                    final PsiElement marker    = packages.getFirstChild();
                    final LeafPsiElement comma = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                    if (marker != null && comma != null) {
                        final PsiElement advisories = new JsonElementGenerator(project)
                                .createObject("\"roave/security-advisories\": \"dev-master\"")
                                .getPropertyList().get(0);
                        marker.getParent().addAfter(comma, marker);
                        marker.getParent().addAfter(advisories, marker);
                    }
                }
            }
        }
    }
}
