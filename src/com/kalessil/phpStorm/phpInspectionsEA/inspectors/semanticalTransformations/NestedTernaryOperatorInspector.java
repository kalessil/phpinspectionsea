package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class NestedTernaryOperatorInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Nested ternary operator shall not be used";

    @NotNull
    public String getShortName() {
        return "NestedTernaryOperatorInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (objCondition instanceof TernaryExpression) {
                    holder.registerProblem(objCondition, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                PsiElement objTrueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                if (objTrueVariant instanceof TernaryExpression) {
                    holder.registerProblem(objTrueVariant, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                PsiElement objFalseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                if (objFalseVariant instanceof TernaryExpression) {
                    holder.registerProblem(objFalseVariant, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}