package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
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
        final PsiElement parent     = expression.getParent();
        if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
            /* binary expressions, already wrapped into parentheses can be skipped */
            if (parent instanceof BinaryExpression) {
                final IElementType parentOperator = ((BinaryExpression) parent).getOperationType();
                if (parentOperator != operator && (parentOperator == PhpTokenTypes.opAND || parentOperator == PhpTokenTypes.opOR)) {
                    final String replacement = '(' + expression.getText() + ')';
                    holder.registerProblem(expression, message, new WrapItAsItIsFix(replacement));
                    return true;
                }
            }
            /* assignment dramatically changing precedence */
            else if (OpenapiTypesUtil.isAssignment(parent) && !OpenapiTypesUtil.isStatementImpl(parent.getParent())) {
                final String replacement = '(' + expression.getText() + ')';
                holder.registerProblem(expression, message, new WrapItAsItIsFix(replacement));
                return true;
            }
        } else if (PhpTokenTypes.tsCOMPARE_OPS.contains(operator)) {
            if (OpenapiTypesUtil.isAssignment(parent) && parent.getParent() instanceof If) {
                final AssignmentExpression assignment = (AssignmentExpression) parent;
                final PsiElement assignedValue        = assignment.getValue();
                if (assignedValue != null) {
                    final String value       = assignedValue.getText();
                    final String replacement = assignment.getText().replace(value, '(' + value + ')');
                    holder.registerProblem(parent, message, new WrapItAsItIsFix(replacement));
                    return true;
                }
            } else if (PhpTokenTypes.tsCOMPARE_ORDER_OPS.contains(operator)) {
                final PsiElement left = expression.getLeftOperand();
                if (left instanceof UnaryExpression) {
                    final UnaryExpression candidate = (UnaryExpression) left;
                    if (OpenapiTypesUtil.is(candidate.getOperation(), PhpTokenTypes.opNOT)) {
                        final String value       = candidate.getText();
                        final String replacement = expression.getText().replace(value, '(' + value + ')');
                        holder.registerProblem(expression, message, new WrapItAsItIsFix(replacement));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final class WrapItAsItIsFix extends UseSuggestedReplacementFixer {
        private static final String title = "Wrap with parentheses as it is";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        WrapItAsItIsFix(@NotNull String replacement) {
            super(replacement);
        }
    }
}
