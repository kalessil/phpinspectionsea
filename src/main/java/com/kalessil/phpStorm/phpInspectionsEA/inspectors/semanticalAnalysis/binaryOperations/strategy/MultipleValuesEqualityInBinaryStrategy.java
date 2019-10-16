package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MultipleValuesEqualityInBinaryStrategy {
    private static final String messageAlwaysTrue  = "'%s || %s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s && %s' seems to be always false.";
    private static final String messageNoEffect    = "'%s' seems to have no effect due to '%s'.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator != null && (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR)) {
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<BinaryExpression> fragments = extractFragments(expression, operator);
                result                                 = fragments.size() > 1 && analyze(fragments, operator, holder);
                fragments.clear();
            }
        }
        return result;
    }

    private static boolean analyze(
            @NotNull List<BinaryExpression> filtered,
            @NotNull IElementType operator,
            @NotNull ProblemsHolder holder
    ) {
        boolean result                                                 = false;
        final Map<BinaryExpression, Pair<Pair<PsiElement, PsiElement>, Boolean>> details = new HashMap<>();
        for (final BinaryExpression fragment : filtered) {
            final Pair<Pair<PsiElement, PsiElement>, Boolean> current = details.computeIfAbsent(fragment, MultipleValuesEqualityInBinaryStrategy::extract);
            if (current != null) {
                boolean reachedStartingPoint = false;
                for (final BinaryExpression match : filtered) {
                    reachedStartingPoint = reachedStartingPoint || match == fragment;
                    if (reachedStartingPoint && match != fragment) {
                        final Pair<Pair<PsiElement, PsiElement>, Boolean> next = details.computeIfAbsent(match, MultipleValuesEqualityInBinaryStrategy::extract);
                        if (next != null) {
                            if (operator == PhpTokenTypes.opOR) {
                                if (current.second && isConstantCondition(current, next)) {
                                    if (isSameValue(current, next)) {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageNoEffect), match.getText(), fragment.getText()),
                                                ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                        );
                                    } else {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageAlwaysTrue), fragment.getText(), match.getText())
                                        );
                                    }
                                    result = true;
                                } else if (isNoEffectCondition(current, next)) {
                                    if (isSameValue(current, next)) {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageAlwaysTrue), fragment.getText(), match.getText())
                                        );
                                    } else {
                                        final PsiElement target = current.second ? match : fragment;
                                        holder.registerProblem(
                                                target,
                                                String.format(ReportingUtil.wrapReportedMessage(messageNoEffect), target.getText(), (target == fragment ? match : fragment).getText()),
                                                ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                        );
                                    }
                                    result = true;
                                }
                            } else {
                                if (!current.second && isConstantCondition(current, next)) {
                                    if (isSameValue(current, next)) {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageNoEffect), match.getText(), fragment.getText()),
                                                ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                        );
                                    } else {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageAlwaysFalse), fragment.getText(), match.getText())
                                        );
                                    }
                                    result = true;
                                } else if (isNoEffectCondition(current, next)) {
                                    if (isSameValue(current, next)) {
                                        holder.registerProblem(
                                                match,
                                                String.format(ReportingUtil.wrapReportedMessage(messageAlwaysFalse), fragment.getText(), match.getText())
                                        );
                                    } else {
                                        final PsiElement target = current.second ? fragment : match;
                                        holder.registerProblem(target,
                                                String.format(ReportingUtil.wrapReportedMessage(messageNoEffect), target.getText(), (target == fragment ? match : fragment).getText()),
                                                ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                        );
                                    }
                                    result = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isConstantCondition(@NotNull Pair<Pair<PsiElement, PsiElement>, Boolean> current, Pair<Pair<PsiElement, PsiElement>, Boolean> next) {
        if (current.second == next.second) {
            final Map<PsiElement, List<PsiElement>> groups = groupValues(current, next);
            if (!groups.isEmpty()) {
                final boolean result = groups.values().stream()
                        .anyMatch(l -> l.size() == 2 && (OpenapiEquivalenceUtil.areEqual(l.get(0), l.get(1)) || l.stream().allMatch(MultipleValuesEqualityInBinaryStrategy::isValueType)));
                groups.values().forEach(List::clear);
                groups.clear();
                return result;
            }
        }
        return false;
    }

    private static boolean isNoEffectCondition(@NotNull Pair<Pair<PsiElement, PsiElement>, Boolean> current, Pair<Pair<PsiElement, PsiElement>, Boolean> next) {
        if (current.second != next.second) {
            final Map<PsiElement, List<PsiElement>> groups = groupValues(current, next);
            if (!groups.isEmpty()) {
                final boolean result = groups.values().stream()
                        .anyMatch(l -> l.size() == 2 && (OpenapiEquivalenceUtil.areEqual(l.get(0), l.get(1)) || l.stream().allMatch(MultipleValuesEqualityInBinaryStrategy::isValueType)));
                groups.values().forEach(List::clear);
                groups.clear();
                return result;
            }
        }
        return false;
    }

    private static boolean isSameValue(@NotNull Pair<Pair<PsiElement, PsiElement>, Boolean> current, Pair<Pair<PsiElement, PsiElement>, Boolean> next) {
        final Map<PsiElement, List<PsiElement>> groups = groupValues(current, next);
        if (!groups.isEmpty()) {
            final boolean result = groups.values().stream().anyMatch(l -> l.size() == 2 && OpenapiEquivalenceUtil.areEqual(l.get(0), l.get(1)));
            groups.values().forEach(List::clear);
            groups.clear();
            return result;
        }
        return false;
    }

    private static boolean isValueType(@NotNull PsiElement element) {
        return  element instanceof StringLiteralExpression ||
                element instanceof ConstantReference ||
                element instanceof ClassConstantReference ||
                element instanceof ArrayCreationExpression ||
                OpenapiTypesUtil.isNumber(element);
    }

    @NotNull
    private static Map<PsiElement, List<PsiElement>> groupValues(@NotNull Pair<Pair<PsiElement, PsiElement>, Boolean> current, Pair<Pair<PsiElement, PsiElement>, Boolean> next) {
        final Map<PsiElement, List<PsiElement>> groups = new HashMap<>();
        Stream.of(current, next).forEach(source -> {
            if (!isValueType(source.first.first)) {
                final PsiElement key = groups.keySet().stream().filter(k -> OpenapiEquivalenceUtil.areEqual(k, source.first.first)).findFirst().orElse(source.first.first);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(source.first.second);
            }
            if (!isValueType(source.first.second)) {
                final PsiElement key = groups.keySet().stream().filter(k -> OpenapiEquivalenceUtil.areEqual(k, source.first.second)).findFirst().orElse(source.first.second);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(source.first.first);
            }
        });

        return groups;
    }

    @Nullable
    private static Pair<Pair<PsiElement, PsiElement>, Boolean> extract(@NotNull BinaryExpression source) {
        Pair<Pair<PsiElement, PsiElement>, Boolean> result = null;
        final IElementType operator      = source.getOperationType();
        if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opNOT_EQUAL) {
            final PsiElement valueProbe = ExpressionSemanticUtil.getExpressionTroughParenthesis(source.getRightOperand());
            final PsiElement container  = valueProbe != null && isValueType(valueProbe) ? ExpressionSemanticUtil.getExpressionTroughParenthesis(source.getLeftOperand()) : valueProbe;
            if (container != null) {
                final PsiElement value = OpenapiElementsUtil.getSecondOperand(source, container);
                if (value != null && (value instanceof Variable || isValueType(value))) {
                    result = new Pair<>(new Pair<>(container, value), operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opNOT_EQUAL);
                }
            }
        }
        return result;
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
