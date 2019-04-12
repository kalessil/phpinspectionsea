package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
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
    private static final String messagePatternUnary = "The operation results to '%s', the right operator can be omitted.";
    private static final String messagePatternCall  = "Due to '%s' used as left operand, using '?:' instead of '??' would make more sense.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (expression.getOperationType() == PhpTokenTypes.opCOALESCE) {
            final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            if (left instanceof UnaryExpression) {
                final PsiElement operation = ((UnaryExpression) left).getOperation();
                if (operation != null) {
                    final IElementType operator = operation.getNode().getElementType();
                    if (result = (operator == PhpTokenTypes.opNOT || PhpTokenTypes.tsCAST_OPS.contains(operator))) {
                        holder.registerProblem(left, String.format(messagePatternUnary, left.getText()));
                    }
                }
            } if (left instanceof FunctionReference) {
                final PhpType resolved = OpenapiResolveUtil.resolveType((FunctionReference) left, holder.getProject());
                if (resolved != null && !resolved.hasUnknown()) {
                    final boolean isTarget = resolved.getTypes().stream().noneMatch(t -> Types.getType(t).equals(Types.strString));
                    if (isTarget) {
                        holder.registerProblem(left, String.format(messagePatternCall, left.getText()));
                    }
                }
            }
        }
        return result;
    }
}
