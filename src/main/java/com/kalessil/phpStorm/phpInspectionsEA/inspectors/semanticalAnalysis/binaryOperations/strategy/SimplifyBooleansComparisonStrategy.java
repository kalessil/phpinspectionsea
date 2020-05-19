package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    private static final String messagePatternOr  = "'(%s) || (%s)' is the same as '(bool) %s %s (bool) %s', please review the conditions.";
    private static final String messagePatternAnd = "'(%s) && (%s)' is the same as '(bool) %s !== (bool) %s', please review the conditions.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator == PhpTokenTypes.opOR) {
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<BinaryExpression> fragments = extractFragments(expression, operator);
                if (fragments.size() > 1) {
                    final Map<BinaryExpression, Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>>> details = new HashMap<>();
                    for (final BinaryExpression fragment : fragments) {
                        final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> current = details.computeIfAbsent(fragment, (f) -> extract(f, PhpTokenTypes.opAND));
                        if (current != null) {
                            boolean reachedStartingPoint = false;
                            for (final BinaryExpression match : fragments) {
                                reachedStartingPoint = reachedStartingPoint || match == fragment;
                                if (reachedStartingPoint && match != fragment) {
                                    final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> next = details.computeIfAbsent(match, (m) -> extract(m, PhpTokenTypes.opAND));
                                    if (next != null) {
                                        final boolean compare = Stream.of(current.first.second, current.second.second, next.first.second, next.second.second).mapToInt(isInverted -> isInverted ? -1 : 1).sum() == 0;
                                        if (compare && isCoveredByOr(current.first, next) && isCoveredByOr(current.second, next)) {
                                            holder.registerProblem(
                                                    match,
                                                    String.format(
                                                            MessagesPresentationUtil.prefixWithEa(messagePatternOr),
                                                            fragment.getText(),
                                                            match.getText(),
                                                            current.first.first.getText(),
                                                            current.first.second == current.second.second ? "===" : "!==",
                                                            current.second.first.getText()
                                                    )
                                            );
                                            result = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    details.clear();
                }
                fragments.clear();
            }
        } else if (operator == PhpTokenTypes.opAND) {
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<BinaryExpression> fragments = extractFragments(expression, operator);
                if (fragments.size() > 1) {
                    final Map<BinaryExpression, Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>>> details = new HashMap<>();
                    for (final BinaryExpression fragment : fragments) {
                        final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> current = details.computeIfAbsent(fragment, (f) -> extract(f, PhpTokenTypes.opOR));
                        if (current != null) {
                            boolean reachedStartingPoint = false;
                            for (final BinaryExpression match : fragments) {
                                reachedStartingPoint = reachedStartingPoint || match == fragment;
                                if (reachedStartingPoint && match != fragment) {
                                    final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> next = details.computeIfAbsent(match, (m) -> {
                                        final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> probe = extract(m, PhpTokenTypes.opNOT_IDENTICAL);
                                        return probe == null ? extract(m, PhpTokenTypes.opNOT_EQUAL) : probe;
                                    });
                                    if (next != null) {
                                        final boolean compare = Stream.of(current.first.second, current.second.second, next.first.second, next.second.second).mapToInt(isInverted -> isInverted ? 1 : 0).sum() == 1;
                                        if (compare && isCoveredByAnd(current.first, next, false) && isCoveredByAnd(current.second, next, true)) {
                                            holder.registerProblem(
                                                    match,
                                                    String.format(
                                                            MessagesPresentationUtil.prefixWithEa(messagePatternAnd),
                                                            fragment.getText(),
                                                            match.getText(),
                                                            current.first.first.getText(),
                                                            current.second.first.getText()
                                                    )
                                            );
                                            result = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                fragments.clear();
            }
        }
        return result;
    }

    private static boolean isCoveredByOr(@NotNull Pair<PsiElement, Boolean> what, Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> byWhat) {
        return (what.second != byWhat.first.second && OpenapiEquivalenceUtil.areEqual(what.first, byWhat.first.first)) ||
               (what.second != byWhat.second.second && OpenapiEquivalenceUtil.areEqual(what.first, byWhat.second.first));
    }

    private static boolean isCoveredByAnd(@NotNull Pair<PsiElement, Boolean> what, Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> byWhat, boolean expectedInversion) {
        return (! what.second && byWhat.first.second == expectedInversion && OpenapiEquivalenceUtil.areEqual(what.first, byWhat.first.first)) ||
                (! what.second && byWhat.second.second == expectedInversion && OpenapiEquivalenceUtil.areEqual(what.first, byWhat.second.first));
    }

    @Nullable
    private static Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> extract(@NotNull BinaryExpression source, @NotNull IElementType targetOperation) {
        Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> result = null;
        final IElementType operation                                      = source.getOperationType();
        if (operation == targetOperation) {
            final PsiElement left  = source.getLeftOperand();
            final PsiElement right = source.getRightOperand();
            if (left != null && right != null) {
                if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL) {
                    /* pretend we are dealing with <left> === !<right> here */
                    result = new Pair<>(new Pair<>(left, false), new Pair<>(right, true));
                } else {
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

    @NotNull
    private static List<BinaryExpression> extractFragments(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
        /* extract only binary expressions, ignore other condition parts */
        final List<BinaryExpression> result = new ArrayList<>();
        if (binary.getOperationType() == operator) {
            Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                    .map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                    .forEach(expression -> {
                        if (expression instanceof BinaryExpression) {
                            result.addAll(extractFragments((BinaryExpression) expression, operator));
                        }
                    });
        } else {
            result.add(binary);
        }
        return result;
    }
}
