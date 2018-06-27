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
    private static final String message       = "Please add roave/security-advisories:dev-master into require-dev as a firewall for vulnerable components.";
    private static final String useMaster     = "Please use dev-master instead.";
    private static final String useRequireDev = "Dev-packages have no security guaranties, invoke the package via require-dev instead.";

    // Inspection options.
    public boolean REPORT_MISSING_ROAVE_ADVISORIES = true;
    public boolean REPORT_MISPLACED_DEPENDENCIES   = true;
    public final List<String> optionConfiguration  = new ArrayList<>();

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
        if (property != null && property.getValue() instanceof JsonObject) {
            result = property;
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
            boolean hasThirdPartyPackages                                 = false;
            final Map<JsonProperty, JsonStringLiteral> productionPackages = this.getPackages(productionRequire);
            if (!productionPackages.isEmpty()) {
                for (final Map.Entry<JsonProperty, JsonStringLiteral> pair : productionPackages.entrySet()) {
                    final String packageName    = pair.getKey().getName().toLowerCase();
                    final String packageVersion = pair.getValue().getValue().toLowerCase();
                    if (!packageName.isEmpty() && !packageVersion.isEmpty()) {
                        /* identify usage development components */
                        if (REPORT_MISPLACED_DEPENDENCIES && optionConfiguration.contains(packageName)) {
                            holder.registerProblem(pair.getKey().getFirstChild(), useRequireDev);
                        }
                        /* identify usage of third party components */
                        if (!hasThirdPartyPackages && packageName.indexOf('/') != -1) {
                            hasThirdPartyPackages = vendorName == null || !packageName.startsWith(vendorName);
                        }
                    }
                }
                productionPackages.clear();
            }

            if (REPORT_MISSING_ROAVE_ADVISORIES) {
                boolean hasAdvisories                 = false;
                final JsonProperty developmentRequire = this.getPackagesGroup(manifest, "require-dev");
                if (developmentRequire != null) {
                    final Map<JsonProperty, JsonStringLiteral> developmentPackages = this.getPackages(developmentRequire);
                    if (!developmentPackages.isEmpty()) {
                        for (final Map.Entry<JsonProperty, JsonStringLiteral> pair : developmentPackages.entrySet()) {
                            final String packageName    = pair.getKey().getName().toLowerCase();
                            final String packageVersion = pair.getValue().getValue().toLowerCase();
                            if (!packageName.isEmpty() && !packageVersion.isEmpty()) {
                                if (packageName.equals("roave/security-advisories")) {
                                    if (!packageVersion.equals("dev-master")) {
                                        holder.registerProblem(pair.getValue(), useMaster);
                                    }
                                    hasAdvisories = true;
                                    break;
                                }
                            }
                        }
                        developmentPackages.clear();
                    }
                }
                if (!hasAdvisories && hasThirdPartyPackages) {
                    final JsonProperty target = developmentRequire == null ? productionRequire : developmentRequire;
                    holder.registerProblem(target.getFirstChild(), message, new AddAdvisoriesFix(target));
                }
            }
        }

        return holder.getResultsArray();
    }

    private static final class AddAdvisoriesFix implements LocalQuickFix {
        private final SmartPsiElementPointer<JsonProperty> require;

        AddAdvisoriesFix(@NotNull JsonProperty require) {
            super();

            this.require = SmartPointerManager.getInstance(require.getProject()).createSmartPsiElementPointer(require);
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
