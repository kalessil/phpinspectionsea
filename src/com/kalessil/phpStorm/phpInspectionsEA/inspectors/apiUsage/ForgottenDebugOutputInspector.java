package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ForgottenDebugOutputInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "Please ensure this is not forgotten debug output.";
    private final String strTargetFunctions = "print_r,var_export,var_dump";

    @NotNull
    public String getDisplayName() {
        return "API: forgotten debug output";
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
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

                final String strFunction = reference.getName();
                if (null == strFunction) {
                    return;
                }

                final boolean isTargetFunction = strTargetFunctions.contains(strFunction.toLowerCase());
                if (!isTargetFunction) {
                    return;
                }

                holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
