package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class HardcodedConstantValuesStrategy {
    private static final String message = "This makes no sense or enforces the operation result.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result               = false;
        final IElementType operation = expression.getOperationType();
        if (
            PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation) ||
            PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)
        ) {
            final PsiElement left  = expression.getLeftOperand();
            final PsiElement right = expression.getRightOperand();
            if (PhpLanguageUtil.isBoolean(left) || PhpLanguageUtil.isNull(left)) {
                holder.registerProblem(left, message);
                result = true;
            }
            /* no else-if, as it breaks proper processing */
            /* else */ if (PhpLanguageUtil.isBoolean(right) || PhpLanguageUtil.isNull(right)) {
                holder.registerProblem(right, message);
                result = true;
            }
            // TODO: [], '', ""
        }
        return result;
    }
}
