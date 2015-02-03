package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class CallableInLoopTerminationConditionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Callable result shall be stored outside of the loop for better performance";

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFor(For expression) {
                PhpPsiElement[] arrConditions = expression.getConditionalExpressions();
                if (arrConditions.length != 1 || !(arrConditions[0] instanceof BinaryExpression)) {
                    return;
                }

                BinaryExpression objCondition = (BinaryExpression) arrConditions[0];

                PsiElement objRightOperand = objCondition.getRightOperand();
                PsiElement objLeftOperand = objCondition.getLeftOperand();
                if (
                    (objRightOperand instanceof FunctionReference && !(objRightOperand instanceof MethodReference)) ||
                    (objLeftOperand instanceof FunctionReference  && !(objLeftOperand instanceof MethodReference))
                ) {
                    holder.registerProblem(objCondition, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}