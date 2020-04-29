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
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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
    private static final String patternUseOperands = "Can be replaced with '%s' (reduces cyclomatic complexity and cognitive load).";

    @NotNull
    @Override
    public String getShortName() {
        return "SenselessTernaryOperatorInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious ternary operator";
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
                    if (operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL) {
                        final boolean isInverted      = operationType == PhpTokenTypes.opNOT_IDENTICAL;
                        final PsiElement trueVariant  = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
                        final PsiElement falseVariant = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
                        if (trueVariant != null && falseVariant != null) {
                            final PsiElement value   = binary.getRightOperand();
                            final PsiElement subject = binary.getLeftOperand();
                            if (value != null && subject != null) {
                                final boolean isLeftPartReturned = OpenapiEquivalenceUtil.areEqual(value, trueVariant) || OpenapiEquivalenceUtil.areEqual(value, falseVariant);
                                if (isLeftPartReturned) {
                                    final boolean isRightPartReturned = OpenapiEquivalenceUtil.areEqual(subject, falseVariant) || OpenapiEquivalenceUtil.areEqual(subject, trueVariant);
                                    if (isRightPartReturned) {
                                        final String replacement = falseVariant.getText();
                                        holder.registerProblem(
                                                expression,
                                                String.format(MessagesPresentationUtil.prefixWithEa(patternUseOperands), replacement),
                                                new SimplifyFix(replacement)
                                        );
                                    }
                                }
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}