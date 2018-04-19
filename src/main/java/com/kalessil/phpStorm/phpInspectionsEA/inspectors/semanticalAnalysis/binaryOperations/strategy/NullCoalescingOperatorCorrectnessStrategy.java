package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
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

final public class NullCoalescingOperatorCorrectnessStrategy {
    private static final String messagePattern = "The operation results to '%e%', please add missing parentheses.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (expression.getOperationType() == PhpTokenTypes.opCOALESCE) {
            final PsiElement left      = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            final PsiElement operation = left instanceof UnaryExpression ? ((UnaryExpression) left).getOperation() : null;
            if (operation != null && PhpTokenTypes.tsCAST_OPS.contains(operation.getNode().getElementType())) {
                final String message = messagePattern.replace("%e%", left.getText());
                holder.registerProblem(left, message);
                result = true;
            }
        }
        return result;
    }
}
