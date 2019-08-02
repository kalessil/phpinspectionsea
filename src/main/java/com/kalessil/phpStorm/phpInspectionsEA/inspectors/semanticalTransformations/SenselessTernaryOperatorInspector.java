package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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
    @Override
    public String getShortName() {
        return "SenselessTernaryOperatorInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
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
                                OpenapiEquivalenceUtil.areEqual(value, trueVariant) ||
                                OpenapiEquivalenceUtil.areEqual(value, falseVariant)
                            );
                            final boolean isRightPartReturned = (isLeftPartReturned && (
                                OpenapiEquivalenceUtil.areEqual(subject, falseVariant) ||
                                OpenapiEquivalenceUtil.areEqual(subject, trueVariant)
                            ));
                            if (isLeftPartReturned && isRightPartReturned) {
                                final String replacement = falseVariant.getText();
                                holder.registerProblem(
                                    expression,
                                    patternUseOperands.replace("%o%", replacement),
                                    new SimplifyFix(replacement)
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class SimplifyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify the expression";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}