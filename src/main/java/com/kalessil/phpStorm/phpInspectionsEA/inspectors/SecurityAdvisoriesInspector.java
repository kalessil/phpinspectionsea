package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.*;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecurityAdvisoriesInspector extends LocalInspectionTool {
    private static final String message = "Please add roave/security-advisories:dev-master as a firewall for vulnerable components";

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
                        if (((JsonProperty) component).getName().equals("roave/security-advisories")) {
                            return null;
                        }
                    }

                    /* fire error message */
                    ProblemsHolder holder = new ProblemsHolder(manager, file, isOnTheFly);
                    holder.registerProblem(requireProperty.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR);
                    return holder.getResultsArray();
                }

                break;
            }
        }

        /* no children */
        return null;
    }
}
