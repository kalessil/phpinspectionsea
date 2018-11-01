package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
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

final public class MistypedLogicalOperatorsStrategy {
    private static final String messagePattern = "It was probably was intended to use %s here (one of arguments is not an integer).";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator == PhpTokenTypes.opBIT_AND || operator == PhpTokenTypes.opBIT_OR) {
            final PsiElement left   = expression.getLeftOperand();
            final PsiElement right  = expression.getRightOperand();
            if (left != null && right != null) {
                final boolean isTarget            = !isIntegerType(left) || !isIntegerType(right);
                final PsiElement targetExpression = expression.getOperation();
                if (isTarget && targetExpression != null) {
                    result = true;
                    holder.registerProblem(
                            targetExpression,
                            String.format(messagePattern, operator == PhpTokenTypes.opBIT_AND ? "&&" : "||")
                    );
                }
            }
        }
        return result;
    }

    private static boolean isIntegerType(@NotNull PsiElement operand) {
        boolean result = true;
        if (operand instanceof PhpTypedElement) {
            final PhpType type     = OpenapiResolveUtil.resolveType((PhpTypedElement) operand, operand.getProject());
            final PhpType filtered = type == null ? null : type.filterUnknown();
            if (filtered != null && !filtered.isEmpty()) {
                result = filtered.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strInteger));
            }
        }
        return result;
    }
}
