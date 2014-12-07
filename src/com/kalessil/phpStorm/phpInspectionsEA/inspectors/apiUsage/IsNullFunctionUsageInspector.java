package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'is_null(...)' shall be replace with '... === null'";

    @NotNull
    public String getDisplayName() {
        return "API: 'is_null(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                if (intArgumentsCount != 1) {
                    return;
                }

                final String strFunctionName = reference.getName();
                if (strFunctionName == null) {
                    return;
                }

                final boolean isNullFunctionUsed = ("is_null").equals(strFunctionName.toLowerCase());
                if (!isNullFunctionUsed) {
                    return;
                }

                holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}