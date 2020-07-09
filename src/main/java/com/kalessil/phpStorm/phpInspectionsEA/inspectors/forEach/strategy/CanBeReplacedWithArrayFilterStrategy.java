package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

final public class CanBeReplacedWithArrayFilterStrategy extends AbstractStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression) {
        if (expression instanceof If && ! ExpressionSemanticUtil.hasAlternativeBranches((If) expression)) {
            final PsiElement loopSource = foreach.getArray();
            final PsiElement loopIndex  = foreach.getKey();
            final PsiElement loopValue  = foreach.getValue();
            if (loopSource != null && loopIndex != null && loopValue != null) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                    final PsiElement last    = ExpressionSemanticUtil.getLastStatement(body);
                    if (last instanceof PhpUnset) {
                        /* case: if (empty-check($value|$array[$index])) unset($array[$index]); */
                        final PsiElement[] arguments = ((PhpUnset) last).getArguments();
                        if (arguments.length == 1 && isArrayElement(arguments[0], loopSource, loopIndex)) {
                            final PsiElement condition = ((If) expression).getCondition();
                            return condition != null && isEmptyCheck(condition, loopSource, loopIndex, loopValue);
                        }
                    } else if (OpenapiTypesUtil.isStatementImpl(last)) {
                        /* case: if (not-empty-check($value|$array[$index])) $storage[$index] = $value|$array[$index]; // storage: array! */
                        final PsiElement candidate = last.getFirstChild();
                        if (candidate instanceof AssignmentExpression && OpenapiTypesUtil.isAssignment(candidate)) {
                            final AssignmentExpression assignment = (AssignmentExpression) candidate;
                            final PsiElement assignedValue        = assignment.getValue();
                            final PsiElement assignmentStorage    = assignment.getVariable();
                            if (assignedValue != null && assignmentStorage instanceof ArrayAccessExpression && (isArrayElement(assignedValue, loopSource, loopIndex) || isArrayValue(assignedValue, loopValue))) {
                                final ArrayIndex storageKeyHolder = ((ArrayAccessExpression) assignmentStorage).getIndex();
                                final PsiElement storageKey       = storageKeyHolder == null ? null : storageKeyHolder.getValue();
                                if (storageKey != null && OpenapiEquivalenceUtil.areEqual(storageKey, loopIndex)) {
                                    final PsiElement condition = ((If) expression).getCondition();
                                    return condition != null && isNotEmptyCheck(condition, loopSource, loopIndex, loopValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static private boolean isEmptyCheck(
            @NotNull PsiElement condition,
            @NotNull PsiElement source,
            @NotNull PsiElement index,
            @NotNull PsiElement value
    ) {
        if (condition instanceof PhpEmpty) {
            /* empty($value|$array[$index]) */
            final PsiElement[] arguments = ((PhpEmpty) condition).getVariables();
            if (arguments.length == 1) {
                return isArrayElement(arguments[0], source, index) || isArrayValue(arguments[0], value);
            }
        } else if (condition instanceof UnaryExpression) {
            /* ! $value|$array[$index]*/
            final UnaryExpression unary = (UnaryExpression) condition;
            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                if (argument != null) {
                    return isArrayElement(argument, source, index) || isArrayValue(argument, value);
                }
            }
        } else if (condition instanceof BinaryExpression) {
            /* $value|$array[$index] == falsy-value */
            final BinaryExpression binary = (BinaryExpression) condition;
            if (binary.getOperationType() == PhpTokenTypes.opEQUAL) {
                final PsiElement argument;
                if (PhpLanguageUtil.isFalsyValue(binary.getRightOperand())) {
                    argument = binary.getLeftOperand();
                } else if (PhpLanguageUtil.isFalsyValue(binary.getLeftOperand())) {
                    argument = binary.getRightOperand();
                } else {
                    argument = null;
                }
                if (argument != null) {
                    return isArrayElement(argument, source, index) || isArrayValue(argument, value);
                }
            }
        }
        return false;
    }

    static private boolean isNotEmptyCheck(
            @NotNull PsiElement condition,
            @NotNull PsiElement source,
            @NotNull PsiElement index,
            @NotNull PsiElement value
    ) {
        if (condition instanceof UnaryExpression) {
            /* ! empty($value|$array[$index]) */
            final UnaryExpression unary = (UnaryExpression) condition;
            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                if (argument instanceof PhpEmpty) {
                    final PsiElement[] arguments = ((PhpEmpty) argument).getVariables();
                    if (arguments.length == 1) {
                        return isArrayElement(arguments[0], source, index) || isArrayValue(arguments[0], value);
                    }
                }
            }
        } else if (condition instanceof BinaryExpression) {
            /* $value|$array[$index] != falsy-value */
            final BinaryExpression binary = (BinaryExpression) condition;
            if (binary.getOperationType() == PhpTokenTypes.opNOT_EQUAL) {
                final PsiElement argument;
                if (PhpLanguageUtil.isFalsyValue(binary.getRightOperand())) {
                    argument = binary.getLeftOperand();
                } else if (PhpLanguageUtil.isFalsyValue(binary.getLeftOperand())) {
                    argument = binary.getRightOperand();
                } else {
                    argument = null;
                }
                if (argument != null) {
                    return isArrayElement(argument, source, index) || isArrayValue(argument, value);
                }
            }
        } else {
            /* $value|$array[$index] */
            return isArrayElement(condition, source, index) || isArrayValue(condition, value);
        }
        return false;
    }
}
