package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiFile;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jdom.Element;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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

    // Inspection options.
    public final List<String> optionConfiguration = new ArrayList<>();
    public boolean optionConfigurationMigrated;

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(
            (component) -> component.addList("Development packages:",
                                             optionConfiguration, null,
                                             "Adding custom development package...", "Examples: \"phpunit/phpunit\"")
        );
    }

    @Override
    public void readSettings(@NotNull final Element node) {
        super.readSettings(node);

        if (!optionConfigurationMigrated) {
            optionConfiguration.clear();

            /* PhpUnit */
            optionConfiguration.add("phpunit/phpunit");
            optionConfiguration.add("phpunit/dbunit");
            optionConfiguration.add("johnkary/phpunit-speedtrap");
            optionConfiguration.add("symfony/phpunit-bridge");

            /* more dev-packages  */
            optionConfiguration.add("mockery/mockery");
            optionConfiguration.add("behat/behat");
            optionConfiguration.add("phpspec/prophecy");
            optionConfiguration.add("phpspec/phpspec");
            optionConfiguration.add("composer/composer");
            optionConfiguration.add("satooshi/php-coveralls");
            optionConfiguration.add("phpro/grumphp");

            /* SCA tools */
            optionConfiguration.add("friendsofphp/php-cs-fixer");
            optionConfiguration.add("squizlabs/php_codesniffer");
            optionConfiguration.add("phpstan/phpstan");
            optionConfiguration.add("vimeo/psalm");
            optionConfiguration.add("jakub-onderka/php-parallel-lint");
            optionConfiguration.add("slevomat/coding-standard");
            optionConfiguration.add("phpmd/phpmd");
            optionConfiguration.add("pdepend/pdepend");
            optionConfiguration.add("sebastian/phpcpd");
            optionConfiguration.add("povils/phpmnd");

            /* build tools */
            optionConfiguration.add("phing/phing");

            optionConfigurationMigrated = true;
        }
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        /* verify file name and it's validity */
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
                break;
            }

            if (optionConfiguration.contains(packageName)) {
                holder.registerProblem(component.getFirstChild(), useRequireDev);
                continue;
            }

            if (packageName.indexOf('/') != -1) {
                if ((ownPackagePrefix == null) || !packageName.startsWith(ownPackagePrefix)) {
                    hasThirdPartyPackages = true;
                }
            }
        }

        /* fire error message if we have any of 3rd-party packages */
        if (hasThirdPartyPackages && !hasAdvisories) {
            holder.registerProblem(requireProperty.getFirstChild(), message);
        }

        return holder.getResultsArray();
    }
}
