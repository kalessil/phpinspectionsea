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
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SecurityAdvisoriesInspector extends LocalInspectionTool {
    private static final String message       = "Please add roave/security-advisories:dev-latest into require-dev as a firewall for vulnerable components.";
    private static final String useLatest     = "Please use dev-latest instead.";
    private static final String useRequireDev = "Dev-packages have no security guaranties, invoke the package via require-dev instead.";

    // Inspection options.
    public boolean REPORT_MISSING_ROAVE_ADVISORIES = true;
    public boolean REPORT_MISPLACED_DEPENDENCIES   = true;
    public final List<String> optionConfiguration  = new ArrayList<>();

    public static Collection<String> optionConfigurationDefaults() {
        final Collection<String> developmentPackages = new TreeSet<>();

        /* Not included: webmozart/assert, beberlei/assert - can be used in production */

        /* PHPUnit */
        developmentPackages.add("phpunit/phpunit");
        developmentPackages.add("johnkary/phpunit-speedtrap");
        developmentPackages.add("brianium/paratest");
        developmentPackages.add("mybuilder/phpunit-accelerator");
        developmentPackages.add("codedungeon/phpunit-result-printer");
        developmentPackages.add("spatie/phpunit-watcher");

        /* frameworks: Symfony, ZF2, Yii2, Laravel */
        developmentPackages.add("symfony/phpunit-bridge");
        developmentPackages.add("symfony/debug");
        developmentPackages.add("symfony/var-dumper");
        developmentPackages.add("symfony/maker-bundle");
        developmentPackages.add("zendframework/zend-test");
        developmentPackages.add("zendframework/zend-debug");
        developmentPackages.add("yiisoft/yii2-gii");
        developmentPackages.add("yiisoft/yii2-debug");
        developmentPackages.add("orchestra/testbench");
        developmentPackages.add("barryvdh/laravel-debugbar");

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
        developmentPackages.add("phpstan/phpstan");
        developmentPackages.add("vimeo/psalm");
        developmentPackages.add("jakub-onderka/php-parallel-lint");
        developmentPackages.add("squizlabs/php_codesniffer");
        developmentPackages.add("slevomat/coding-standard");
        developmentPackages.add("doctrine/coding-standard");
        developmentPackages.add("phpcompatibility/php-compatibility");
        developmentPackages.add("zendframework/zend-coding-standard");
        developmentPackages.add("yiisoft/yii2-coding-standards");
        developmentPackages.add("wp-coding-standards/wpcs");
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
        developmentPackages.add("kalessil/production-dependencies-guard");

        return developmentPackages;
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Security advisories for Composer packages";
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
    public void readSettings(@NotNull Element node) throws InvalidDataException {
        super.readSettings(node);

        /* re-introduce default packages additionally to user-defined once */
        final Set<String> entries = new HashSet<>(optionConfiguration);
        entries.addAll(optionConfigurationDefaults());

        /* re-fill configuration with unique entries */
        optionConfiguration.clear();
        optionConfiguration.addAll(entries);
        Collections.sort(optionConfiguration);
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

    @Nullable
    private JsonProperty getPackagesGroup(@NotNull JsonObject manifest, @NotNull String name) {
        JsonProperty result         = null;
        final JsonProperty property = manifest.findProperty(name);
        if (property != null) {
            final JsonValue value = property.getValue();
            if (value instanceof JsonObject) {
                result = property;
            }
        }
        return result;
    }

    @NotNull
    private Map<JsonProperty, JsonStringLiteral> getPackages(@NotNull JsonProperty group) {
        Map<JsonProperty, JsonStringLiteral> result = new HashMap<>();
        final JsonValue value                       = group.getValue();
        if (value instanceof JsonObject) {
            for (final JsonProperty entry : ((JsonObject) value).getPropertyList()) {
                final JsonValue version = entry.getValue();
                if (version instanceof JsonStringLiteral) {
                    result.put(entry, (JsonStringLiteral) version);
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
        final JsonObject manifest = (JsonObject) file.getFirstChild();

        /* skip analyzing libraries (we can break minimum stability requirements) */
        if (this.isLibrary(manifest)) {
            return null;
        }

        /* identify package owner; skip analyzing dev-packages */
        final String vendorName = this.getVendorName(manifest);
        if (vendorName != null && optionConfiguration.contains(vendorName)) {
            return null;
        }

        final ProblemsHolder holder          = new ProblemsHolder(manager, file, isOnTheFly);
        final JsonProperty productionRequire = this.getPackagesGroup(manifest, "require");
        if (productionRequire != null) {
            boolean isSecured                                             = false;
            boolean hasThirdPartyPackages                                 = false;
            final Map<JsonProperty, JsonStringLiteral> productionPackages = this.getPackages(productionRequire);
            if (!productionPackages.isEmpty()) {
                for (final Map.Entry<JsonProperty, JsonStringLiteral> pair : productionPackages.entrySet()) {
                    final String packageName    = pair.getKey().getName().toLowerCase();
                    final String packageVersion = pair.getValue().getValue().toLowerCase();
                    if (!packageName.isEmpty() && !packageVersion.isEmpty()) {
                        /* identify usage development components */
                        if (REPORT_MISPLACED_DEPENDENCIES && optionConfiguration.contains(packageName)) {
                            holder.registerProblem(
                                    pair.getKey().getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(useRequireDev)
                            );
                        }
                        /* identify usage of third party components */
                        if (!hasThirdPartyPackages && packageName.indexOf('/') != -1) {
                            hasThirdPartyPackages = vendorName == null || !packageName.startsWith(vendorName);
                        }
                        /* check if already secured */
                        isSecured = isSecured || packageName.equals("sensiolabs/security-checker");
                    }
                }
                productionPackages.clear();
            }

            if (REPORT_MISSING_ROAVE_ADVISORIES) {
                final JsonProperty developmentRequire = this.getPackagesGroup(manifest, "require-dev");
                if (developmentRequire != null) {
                    final Map<JsonProperty, JsonStringLiteral> developmentPackages = this.getPackages(developmentRequire);
                    if (!developmentPackages.isEmpty()) {
                        for (final Map.Entry<JsonProperty, JsonStringLiteral> pair : developmentPackages.entrySet()) {
                            final String packageName    = pair.getKey().getName().toLowerCase();
                            final String packageVersion = pair.getValue().getValue().toLowerCase();
                            if (!packageName.isEmpty() && !packageVersion.isEmpty()) {
                                if (packageName.equals("roave/security-advisories")) {
                                    if (!packageVersion.equals("dev-latest")) {
                                        holder.registerProblem(
                                                pair.getValue(),
                                                MessagesPresentationUtil.prefixWithEa(useLatest)
                                        );
                                    }
                                    isSecured = true;
                                    break;
                                }
                                isSecured = isSecured || packageName.equals("sensiolabs/security-checker");
                            }
                        }
                        developmentPackages.clear();
                    }
                }
                if (!isSecured && hasThirdPartyPackages) {
                    holder.registerProblem(
                            productionRequire.getFirstChild(),
                            MessagesPresentationUtil.prefixWithEa(message),
                            new AddAdvisoriesFix(holder.getProject(), productionRequire, developmentRequire)
                    );
                }
            }
        }

        return holder.getResultsArray();
    }

    private static final class AddAdvisoriesFix implements LocalQuickFix {
        private final SmartPsiElementPointer<JsonProperty> productionRequire;
        private final SmartPsiElementPointer<JsonProperty> developmentRequire;

        AddAdvisoriesFix(@NotNull Project project, @NotNull JsonProperty productionRequire, @Nullable JsonProperty developmentRequire) {
            super();
            final SmartPointerManager manager = SmartPointerManager.getInstance(project);
            this.productionRequire  = manager.createSmartPsiElementPointer(productionRequire);
            this.developmentRequire = developmentRequire == null ? null : manager.createSmartPsiElementPointer(developmentRequire);
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
            final JsonProperty productionRequire = this.productionRequire.getElement();
            final LeafPsiElement comma           = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
            if (productionRequire != null && comma != null && !project.isDisposed()) {
                final JsonProperty developmentRequire = this.developmentRequire == null ? null : this.developmentRequire.getElement();
                if (developmentRequire == null) {
                    final PsiElement advisories = new JsonElementGenerator(project)
                            .createObject("\"require-dev\": {\"roave/security-advisories\": \"dev-latest\"}")
                            .getPropertyList().get(0);
                    productionRequire.getParent().addAfter(advisories, productionRequire);
                    productionRequire.getParent().addAfter(comma, productionRequire);
                } else {
                    final PsiElement packages = developmentRequire.getValue();
                    if (packages instanceof JsonObject) {
                        final PsiElement marker     = packages.getFirstChild();
                        final PsiElement advisories = new JsonElementGenerator(project)
                                .createObject("\"roave/security-advisories\": \"dev-latest\"")
                                .getPropertyList().get(0);
                        marker.getParent().addAfter(comma, marker);
                        marker.getParent().addAfter(advisories, marker);
                    }
                }
            }
        }
    }
}
