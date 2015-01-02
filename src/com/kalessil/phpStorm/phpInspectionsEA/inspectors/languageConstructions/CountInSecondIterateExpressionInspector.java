package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.For;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class CountInSecondIterateExpressionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Store 'count()' result outside the loop," +
            " instead of calling it on each iteration.";

    @NotNull
    public String getDisplayName() {
        return "API: 'count(...)' calls in loops termination condition";
    }

    @NotNull
    public String getShortName() {
        return "CountInSecondIterateExpressionInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFor(For forStatement) {
                PhpPsiElement[] arrConditions = forStatement.getConditionalExpressions();
                if (arrConditions.length != 1 || !(arrConditions[0] instanceof BinaryExpression)) {
                    return;
                }

                PhpPsiElement objCondition = arrConditions[0];
                PsiElement objRightOperand = ((BinaryExpression) objCondition).getRightOperand();
                if (!(objRightOperand instanceof FunctionReference)) {
                    return;
                }

                String strFunctionName = ((FunctionReference) objRightOperand).getName();
                if (null == strFunctionName || !strFunctionName.toLowerCase().equals("count")) {
                    return;
                }

                holder.registerProblem(objRightOperand, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
