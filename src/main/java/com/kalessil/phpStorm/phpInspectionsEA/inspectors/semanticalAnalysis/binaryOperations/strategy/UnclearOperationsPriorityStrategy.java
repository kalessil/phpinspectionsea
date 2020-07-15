package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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

    private static final Set<IElementType> ternarySafeOperations = new HashSet<>();
    static {
        ternarySafeOperations.add(PhpTokenTypes.opAND);
        ternarySafeOperations.add(PhpTokenTypes.opOR);
        ternarySafeOperations.add(PhpTokenTypes.opIDENTICAL);
        ternarySafeOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        ternarySafeOperations.add(PhpTokenTypes.opEQUAL);
        ternarySafeOperations.add(PhpTokenTypes.opNOT_EQUAL);
        ternarySafeOperations.add(PhpTokenTypes.opGREATER);
        ternarySafeOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        ternarySafeOperations.add(PhpTokenTypes.opLESS);
        ternarySafeOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);
        ternarySafeOperations.add(PhpTokenTypes.kwINSTANCEOF);
        ternarySafeOperations.add(PhpTokenTypes.opSPACESHIP);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent = expression.getParent();

        /* badly structured hack: merge into SuspiciousTernaryOperatorInspector patterns */
        if (parent instanceof TernaryExpression && apply((TernaryExpression) parent, expression, holder)) {
            return true;
        }

        final IElementType operator = expression.getOperationType();
        if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
            /* binary expressions, already wrapped into parentheses can be skipped */
            if (parent instanceof BinaryExpression) {
                final IElementType parentOperator = ((BinaryExpression) parent).getOperationType();
                if (parentOperator != operator) {
                    final boolean isTarget = parentOperator == PhpTokenTypes.opAND || parentOperator == PhpTokenTypes.opOR;
                    if (isTarget) {
                        final String replacement = '(' + expression.getText() + ')';
                        holder.registerProblem(
                                expression,
                                MessagesPresentationUtil.prefixWithEa(message),
                                new WrapItAsItIsFix(replacement)
                        );
                        return true;
                    }
                }
            }
            /* assignment dramatically changing precedence */
            else if (OpenapiTypesUtil.isAssignment(parent) && ! OpenapiTypesUtil.isStatementImpl(parent.getParent())) {
                final String replacement = '(' + expression.getText() + ')';
                holder.registerProblem(
                        expression,
                        MessagesPresentationUtil.prefixWithEa(message),
                        new WrapItAsItIsFix(replacement)
                );
                return true;
            }
        } else if (PhpTokenTypes.tsCOMPARE_OPS.contains(operator)) {
            if (OpenapiTypesUtil.isAssignment(parent) && parent.getParent() instanceof If) {
                final AssignmentExpression assignment = (AssignmentExpression) parent;
                final PsiElement assignedValue        = assignment.getValue();
                if (assignedValue != null) {
                    final String value       = assignedValue.getText();
                    final String replacement = assignment.getText().replace(value, '(' + value + ')');
                    holder.registerProblem(
                            parent,
                            MessagesPresentationUtil.prefixWithEa(message),
                            new WrapItAsItIsFix(replacement)
                    );
                    return true;
                }
            } else if (
                    operator == PhpTokenTypes.opEQUAL ||
                    operator == PhpTokenTypes.opIDENTICAL ||
                    (operator != PhpTokenTypes.opSPACESHIP && PhpTokenTypes.tsCOMPARE_ORDER_OPS.contains(operator))
            ) {
                final PsiElement left = expression.getLeftOperand();
                if (left instanceof UnaryExpression) {
                    final UnaryExpression candidate = (UnaryExpression) left;
                    if (OpenapiTypesUtil.is(candidate.getOperation(), PhpTokenTypes.opNOT)) {
                        final String value       = candidate.getText();
                        final String replacement = expression.getText().replace(value, '(' + value + ')');
                        holder.registerProblem(
                                expression,
                                MessagesPresentationUtil.prefixWithEa(message),
                                new WrapItAsItIsFix(replacement)
                        );
                        return true;
                    }
                }
            }
        } else if (operator == PhpTokenTypes.opCOALESCE) {
            final PsiElement right = expression.getRightOperand();
            if (right instanceof TernaryExpression && ((TernaryExpression) right).isShort()) {
                holder.registerProblem(
                        right,
                        MessagesPresentationUtil.prefixWithEa(message),
                        new WrapItAsItIsFix('(' + right.getText() + ')')
                );
                return true;
            }
            if (parent instanceof TernaryExpression && ((TernaryExpression) parent).isShort()) {
                holder.registerProblem(
                        expression,
                        MessagesPresentationUtil.prefixWithEa(message),
                        new WrapItAsItIsFix('(' + expression.getText() + ')')
                );
                return true;
            }
        } else if (PhpTokenTypes.tsLIT_OPS.contains(operator)) {
            /* case: literal and, or, xor operation with ternary at right side */
            final PsiElement candidate = expression.getRightOperand();
            if (candidate instanceof TernaryExpression) {
                holder.registerProblem(
                        candidate,
                        MessagesPresentationUtil.prefixWithEa(message),
                        new WrapItAsItIsFix(String.format("(%s)", candidate.getText()))
                );
                return true;
            }
        }
        return false;
    }

    private static boolean apply(@NotNull TernaryExpression ternary, @NotNull BinaryExpression condition, @NotNull ProblemsHolder holder) {
        /* case: binary in alternative short ternary branch */
        if (ternary.isShort() && ternary.getFalseVariant() == condition) {
            holder.registerProblem(
                    condition,
                    MessagesPresentationUtil.prefixWithEa(message),
                    new WrapItAsItIsFix(String.format("(%s)", condition.getText()))
            );
            return true;
        }

        /* case: operations which might produce a value as not expected */
        if (! (ternary.getCondition() instanceof ParenthesizedExpression) && ! ternarySafeOperations.contains(condition.getOperationType())) {
            holder.registerProblem(
                    condition,
                    MessagesPresentationUtil.prefixWithEa(message),
                    new WrapItAsItIsFix(String.format("(%s)", condition.getText()))
            );
            return true;
        }

        return false;
    }

    private static final class WrapItAsItIsFix extends UseSuggestedReplacementFixer {
        private static final String title = "Wrap with parentheses as it is";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        WrapItAsItIsFix(@NotNull String replacement) {
            super(replacement);
        }
    }
}
