package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if (expression.getOperationType() == PhpTokenTypes.opOR) {
            final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            if (left instanceof BinaryExpression) {
                final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> leftParts = extract((BinaryExpression) left);
                if (leftParts != null) {
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
                    if (right instanceof BinaryExpression) {
                        final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> rightParts = extract((BinaryExpression) right);
                        if (rightParts != null) {
                            /* ensure same amount of arguments with and without inversion */
                            final int checkSum = Stream.of(leftParts.first.second, leftParts.second.second, rightParts.first.second, rightParts.second.second).mapToInt(isInverted -> isInverted ? -1 : 1).sum();
                            if (checkSum == 0) {
                                holder.registerProblem(expression, "Check it!message");
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private static Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> extract(@NotNull BinaryExpression source) {
        Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> result = null;
        if (source.getOperationType() == PhpTokenTypes.opAND) {
            final PsiElement left  = source.getLeftOperand();
            final PsiElement right = source.getRightOperand();
            if (left != null && right != null) {
                final List<Pair<PsiElement, Boolean>> parts = Stream.of(left, right)
                        .map(part -> {
                            final PsiElement falsyProbe = extractFalsyValue(part);
                            final PsiElement value      = falsyProbe != null ? falsyProbe : extractTruthyValue(part);
                            return value == null ? null : new Pair<>(value, falsyProbe != null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (parts.size() == 2) {
                    result = new Pair<>(parts.get(0), parts.get(1));
                }
                parts.clear();
            }
        }
        return result;
    }

    @Nullable
    private static PsiElement extractFalsyValue(@Nullable PsiElement expression) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context */
        expression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression);
        if (expression != null) {
            if (expression instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) expression;
                final IElementType operation  = binary.getOperationType();
                if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opEQUAL) {
                    final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                    if (left != null && right != null && Stream.of(left, right).anyMatch(PhpLanguageUtil::isFalse)) {
                        return PhpLanguageUtil.isFalse(right) ? left : right;
                    }
                } else if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL) {
                    final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                    if (left != null && right != null && Stream.of(left, right).anyMatch(PhpLanguageUtil::isTrue)) {
                        return PhpLanguageUtil.isTrue(right) ? left : right;
                    }
                }
            } else if (expression instanceof UnaryExpression) {
                final UnaryExpression unary = (UnaryExpression) expression;
                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                    final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                    if (!(argument instanceof PhpEmpty) && !(argument instanceof PhpIsset)) {
                        return argument;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static PsiElement extractTruthyValue(@Nullable PsiElement expression) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context */
        expression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression);
        if (expression != null) {
            if (expression instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) expression;
                final IElementType operation  = binary.getOperationType();
                if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opEQUAL) {
                    final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                    if (left != null && right != null && Stream.of(left, right).anyMatch(PhpLanguageUtil::isTrue)) {
                        return PhpLanguageUtil.isTrue(right) ? left : right;
                    }
                } else if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL) {
                    final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                    if (left != null && right != null && Stream.of(left, right).anyMatch(PhpLanguageUtil::isFalse)) {
                        return PhpLanguageUtil.isFalse(right) ? left : right;
                    }
                }
            } else if (expression instanceof UnaryExpression) {
                final UnaryExpression unary = (UnaryExpression) expression;
                final PsiElement operation  = unary.getOperation();
                if (operation != null && PhpTokenTypes.CAST_OPERATORS.contains(operation.getNode().getElementType())) {
                    return ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                }
            } else {
                if (!(expression instanceof PhpEmpty) && !(expression instanceof PhpIsset)) {
                    return expression;
                }
            }
        }
        return null;
    }
}
