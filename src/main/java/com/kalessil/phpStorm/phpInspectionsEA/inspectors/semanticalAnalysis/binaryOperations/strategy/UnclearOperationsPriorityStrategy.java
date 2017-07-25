package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
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

final public class UnclearOperationsPriorityStrategy {
    private static final String message = "Operations priority might differ from what you expect: please wrap needed with '(...)'.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final IElementType operator = expression.getOperationType();
        if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
            final PsiElement parent = expression.getParent();
            /* binary expressions, already wrapped into parenthesises can be skipped */
            if (parent instanceof BinaryExpression) {
                final IElementType parentOperator = ((BinaryExpression) parent).getOperationType();
                if (parentOperator != operator && (parentOperator == PhpTokenTypes.opAND || parentOperator == PhpTokenTypes.opOR)) {
                    holder.registerProblem(expression, message);
                    return true;
                }
            }
            /* assignment dramatically changing precedence */
            else if (OpenapiTypesUtil.isAssignment(parent) && parent.getParent().getClass() != StatementImpl.class) {
                holder.registerProblem(expression, message);
                return true;
            }
        }
        return false;
    }
}
