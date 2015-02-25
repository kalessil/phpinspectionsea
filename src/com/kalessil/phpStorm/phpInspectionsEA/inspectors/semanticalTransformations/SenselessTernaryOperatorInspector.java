package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class SenselessTernaryOperatorInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Can be replaced with comparison operand";

    @NotNull
    public String getShortName() {
        return ...;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PsiElement objTrueVariant = expression.getTrueVariant();
                final PsiElement objFalseVariant = expression.getFalseVariant();
                final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (null == objTrueVariant || null == objFalseVariant || null == objCondition) {
                    return;
                }

                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }
                PsiElement objOperation = ((BinaryExpression) objCondition).getOperation();
                if (null == objOperation) {
                    return;
                }

                IElementType objOperationType = objOperation.getNode().getElementType();
                final boolean isTargetOperation = (
                    objOperationType == PhpTokenTypes.opEQUAL ||
                    objOperationType == PhpTokenTypes.opIDENTICAL ||
                    objOperationType == PhpTokenTypes.opNOT_EQUAL ||
                    objOperationType == PhpTokenTypes.opNOT_IDENTICAL
                );
                if (!isTargetOperation) {
                    return;
                }

                PsiElement objLeftOperand = ((BinaryExpression) objCondition).getLeftOperand();
                PsiElement objRightOperand = ((BinaryExpression) objCondition).getRightOperand();
                if (null == objLeftOperand || null == objRightOperand) {
                    return;
                }

                final boolean isLeftPartReturned = (
                    PsiEquivalenceUtil.areElementsEquivalent(objLeftOperand, objTrueVariant) ||
                    PsiEquivalenceUtil.areElementsEquivalent(objLeftOperand, objFalseVariant)
                );
                final boolean isRightPartReturned = (isLeftPartReturned && (
                    PsiEquivalenceUtil.areElementsEquivalent(objRightOperand, objFalseVariant) ||
                    PsiEquivalenceUtil.areElementsEquivalent(objRightOperand, objTrueVariant)
                ));
                if (!isLeftPartReturned || !isRightPartReturned) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}