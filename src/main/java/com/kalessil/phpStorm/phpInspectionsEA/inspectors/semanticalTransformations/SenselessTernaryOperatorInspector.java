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
    private static final String messageUseOperands       = "Can be replaced with a compared operand";
    private static final String messageOperandsIdentical = "True and false variants are identical, the ternary makes no sense";

    @NotNull
    public String getShortName() {
        return "SenselessTernaryOperatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PsiElement trueVariant  = expression.getTrueVariant();
                final PsiElement falseVariant = expression.getFalseVariant();
                final PsiElement condition    = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                /* TODO: resolve type of other expressions */
                if (null == trueVariant || null == falseVariant || !(condition instanceof BinaryExpression)) {
                    return;
                }

                final BinaryExpression binaryExpression = (BinaryExpression) condition;
                final PsiElement operation              = binaryExpression.getOperation();
                if (null == operation) {
                    return;
                }

                final IElementType operationType = operation.getNode().getElementType();
                final boolean isTargetOperation = (
                    operationType == PhpTokenTypes.opEQUAL     || operationType == PhpTokenTypes.opIDENTICAL ||
                    operationType == PhpTokenTypes.opNOT_EQUAL || operationType == PhpTokenTypes.opNOT_IDENTICAL
                );
                if (!isTargetOperation) {
                    return;
                }

                final PsiElement leftOperand  = binaryExpression.getLeftOperand();
                final PsiElement rightOperand = binaryExpression.getRightOperand();
                if (null == leftOperand || null == rightOperand) {
                    return;
                }

                final boolean isLeftPartReturned = (
                    PsiEquivalenceUtil.areElementsEquivalent(leftOperand, trueVariant) ||
                    PsiEquivalenceUtil.areElementsEquivalent(leftOperand, falseVariant)
                );
                final boolean isRightPartReturned = (isLeftPartReturned && (
                    PsiEquivalenceUtil.areElementsEquivalent(rightOperand, falseVariant) ||
                    PsiEquivalenceUtil.areElementsEquivalent(rightOperand, trueVariant)
                ));
                if (!isLeftPartReturned || !isRightPartReturned) {
                    return;
                }

                holder.registerProblem(expression, messageUseOperands, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}