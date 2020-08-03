package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ConcatenationWithArrayStrategy {
    private static final String message = "Concatenation with an array doesn't make much sense here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement operation = expression.getOperation();
        if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opCONCAT)) {
            final boolean isTarget = expression.getLeftOperand() instanceof ArrayCreationExpression ||
                                     expression.getRightOperand() instanceof ArrayCreationExpression;
            if (isTarget) {
                holder.registerProblem(
                        operation,
                        MessagesPresentationUtil.prefixWithEa(message)
                );
                return true;
            }
        }
        return false;
    }
}
