package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SimplifyBooleansComparisonStrategy {
    private static final String messageIdentical    = "'(X && Y) || (!X && !Y)' -> '(bool) X === (bool) Y'";
    private static final String messageNotIdentical = "'(X && !Y) || (!X && Y)' -> '(bool) X !== (bool) Y'";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        // NOTE: not a nested one; see UnnecessaryEmptinessCheckInspector for details
        // NOTE: parts can be separated by other binaries or be swapped as well

        if (expression.getOperationType() == PhpTokenTypes.opOR) {
            final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            if (left instanceof BinaryExpression) {
                final BinaryExpression leftBinary = (BinaryExpression) left;
                if (leftBinary.getOperationType() == PhpTokenTypes.opAND) {
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
                    if (right instanceof BinaryExpression) {
                        final BinaryExpression rightBinary = (BinaryExpression) right;
                        if (rightBinary.getOperationType() == PhpTokenTypes.opAND) {

                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private static PsiElement extractFalsyValue(@Nullable PsiElement expression, @NotNull ProblemsHolder holder) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context
            - ! ..., but not !empty(), !isset()
            - ... === false
            - ... == false
            - ... !== true
            - ... != true
            - yoda notation
        */
        return null;
    }

    @Nullable
    private static PsiElement extractTruthyValue(@Nullable PsiElement expression, @NotNull ProblemsHolder holder) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context
            - ..., but not isset(), empty()
            - ... === true
            - ... == true
            - ... !== false
            - ... != false
            - (bool) ...
            - yoda notation
        */
        return null;
    }
}
