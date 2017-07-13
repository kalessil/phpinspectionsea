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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SenselessTernaryOperatorInspector extends BasePhpInspection {
    private static final String patternUseOperands = "Can be replaced with '%o%'.";

    @NotNull
    public String getShortName() {
        return "SenselessTernaryOperatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary    = (BinaryExpression) condition;
                    final IElementType operationType = binary.getOperationType();
                    final boolean isTargetOperation
                        = operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL;
                    if (isTargetOperation) {
                        final boolean isInverted      = operationType == PhpTokenTypes.opNOT_IDENTICAL;
                        final PsiElement trueVariant  = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
                        final PsiElement falseVariant = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
                        final PsiElement value        = binary.getRightOperand();
                        final PsiElement subject      = binary.getLeftOperand();
                        if (trueVariant != null && falseVariant != null && value != null && subject != null) {
                            final boolean isLeftPartReturned = (
                                PsiEquivalenceUtil.areElementsEquivalent(value, trueVariant) ||
                                PsiEquivalenceUtil.areElementsEquivalent(value, falseVariant)
                            );
                            final boolean isRightPartReturned = (isLeftPartReturned && (
                                PsiEquivalenceUtil.areElementsEquivalent(subject, falseVariant) ||
                                PsiEquivalenceUtil.areElementsEquivalent(subject, trueVariant)
                            ));
                            if (isLeftPartReturned && isRightPartReturned) {
                                final String message = patternUseOperands.replace("%o%", falseVariant.getText());
                                holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING);
                            }
                        }
                    }
                }
            }
        };
    }
}