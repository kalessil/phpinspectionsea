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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SecurityAdvisoriesInspector extends LocalInspectionTool {
    private static final String message   = "Please add roave/security-advisories:dev-master as a firewall for vulnerable components.";
    private static final String useMaster = "Please use dev-master instead.";

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
        @Nullable JsonProperty requireProperty = null;
        @Nullable String ownPackagePrefix      = null;
        for (PsiElement option : config.getChildren()) {
            if (option instanceof JsonProperty) {
                final String propertyName = ((JsonProperty) option).getName();
                if (!StringUtil.isEmpty(propertyName)) {
                    if (propertyName.equals("name")) {
                        final JsonValue nameValue = ((JsonProperty) option).getValue();
                        if (nameValue instanceof JsonStringLiteral) {
                            ownPackagePrefix = ((JsonStringLiteral) nameValue).getValue();
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
        if (null != requireProperty && requireProperty.getValue() instanceof JsonObject) {
            final ProblemsHolder holder = new ProblemsHolder(manager, file, isOnTheFly);

            int packagesCount = 0;
            for (PsiElement component : requireProperty.getValue().getChildren()) {
                /* we expect certain structure for package definition */
                if (!(component instanceof JsonProperty)) {
                    continue;
                }

                /* if advisories already there, verify usage of dev-master */
                final JsonProperty componentEntry = (JsonProperty) component;
                final String componentName        = componentEntry.getName();

                if (componentName.equals("roave/security-advisories")) {
                    if (componentEntry.getValue() instanceof JsonStringLiteral) {
                        final JsonStringLiteral version = (JsonStringLiteral) componentEntry.getValue();
                        if (!version.getText().equals("\"dev-master\"")) {
                            holder.registerProblem(version, useMaster, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return holder.getResultsArray();
                        }
                    }

                    return null;
                }

                if (-1 != componentName.indexOf('/')) {
                    /* count packages by other authors */
                    if (null == ownPackagePrefix || !componentName.startsWith(ownPackagePrefix)) {
                        ++packagesCount;
                    }
                }
            }

            /* fire error message if we have any of 3rd-party packages */
            if (packagesCount > 0) {
                holder.registerProblem(requireProperty.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                return holder.getResultsArray();
            }
        }

        /* no children */
        return null;
    }
}
