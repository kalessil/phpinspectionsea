package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.*;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
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
    private static final String message   = "Please add roave/security-advisories:dev-master as a firewall for vulnerable components";
    private static final String useMaster = "Please use dev-master instead";

    @NotNull
    public String getShortName() {
        return "SecurityAdvisoriesInspection";
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        /* verify file name and it's validity */
        if (!file.getName().equals("composer.json")) {
            return null;
        }
        final PsiElement config = file.getFirstChild();
        if(!(config instanceof JsonObject)) {
            return null;
        }

        ProblemsHolder holder = new ProblemsHolder(manager, file, isOnTheFly);
        for (PsiElement option : config.getChildren()) {
            @Nullable JsonProperty requireProperty = null;

            /* find "require" property */
            if (option instanceof JsonProperty) {
                final String propertyName = ((JsonProperty) option).getName();
                if (!StringUtil.isEmpty(propertyName) && propertyName.equals("require")) {
                    requireProperty = (JsonProperty) option;
                }
            }

            /* inspect packages, break afterwards */
            if (null != requireProperty) {
                if (requireProperty.getValue() instanceof JsonObject) {
                    for (PsiElement component : requireProperty.getValue().getChildren()) {
                        /* we expect certain structure for package definition */
                        if (!(component instanceof JsonProperty)) {
                            continue;
                        }
                        /* the package is there already, terminate inspection */
                        JsonProperty componentEntry = (JsonProperty) component;
                        if (componentEntry.getName().equals("roave/security-advisories")) {
                            if (componentEntry.getValue() instanceof JsonStringLiteral) {
                                JsonStringLiteral version = (JsonStringLiteral) componentEntry.getValue();
                                if (!version.getText().equals("\"dev-master\"")) {
                                    holder.registerProblem(version, useMaster, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                    return holder.getResultsArray();
                                }
                            }

                            return null;
                        }
                    }

                    /* fire error message */
                    holder.registerProblem(requireProperty.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return holder.getResultsArray();
                }

                break;
            }
        }

        /* no children */
        return null;
    }
}
