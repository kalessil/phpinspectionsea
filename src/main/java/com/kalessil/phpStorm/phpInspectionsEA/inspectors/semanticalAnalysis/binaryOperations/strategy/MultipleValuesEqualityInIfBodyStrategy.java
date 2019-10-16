package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
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

final public class MultipleValuesEqualityInIfBodyStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (context instanceof If) {
                final GroupStatement scope = ExpressionSemanticUtil.getGroupStatement(context);
                if (scope != null) {
                    final List<BinaryExpression> fragments = extractFragments(expression);
                    result                                 = !fragments.isEmpty() && analyze(fragments, scope, holder);
                    fragments.clear();
                }
            }
        }
        return result;
    }

    private static boolean analyze(@NotNull List<BinaryExpression> filtered, @NotNull GroupStatement scope, @NotNull ProblemsHolder holder) {
        boolean result = false;
        for (final BinaryExpression fragment : filtered) {
            final Pair<Pair<Variable, PsiElement>, Boolean> current = extractBinaryRepresentation(fragment);
            if (current != null) {
                final List<BinaryExpression> candidates = extractBinariesForAnalysis(current.first.first, scope);
                if (candidates != null) {
                    for (final BinaryExpression match : candidates) {
                        final Pair<Pair<Variable, PsiElement>, Boolean> next = extractBinaryRepresentation(match);
                        if (next != null && isConstantCondition(current, next)) {
                            if (current.second == next.second) {
                                if (isSameValue(current, next)) {
                                    holder.registerProblem(match, String.format(ReportingUtil.wrapReportedMessage(messageAlwaysTrue), match.getText()));
                                } else {
                                    holder.registerProblem(match, String.format(ReportingUtil.wrapReportedMessage(messageAlwaysFalse), match.getText()));
                                }
                            } else {
                                if (isSameValue(current, next)) {
                                    holder.registerProblem(match, String.format(ReportingUtil.wrapReportedMessage(messageAlwaysFalse), match.getText()));
                                } else {
                                    holder.registerProblem(match, String.format(ReportingUtil.wrapReportedMessage(messageAlwaysTrue), match.getText()));
                                }
                            }
                        }
                    }
                    candidates.clear();
                }
            }
        }

        return result;
    }

    private static boolean isSameValue(@NotNull Pair<Pair<Variable, PsiElement>, Boolean> current, Pair<Pair<Variable, PsiElement>, Boolean> next) {
        final Map<PsiElement, List<PsiElement>> groups = groupValues(current, next);
        if (!groups.isEmpty()) {
            final boolean result = groups.values().stream().anyMatch(l -> l.size() == 2 && OpenapiEquivalenceUtil.areEqual(l.get(0), l.get(1)));
            groups.values().forEach(List::clear);
            groups.clear();
            return result;
        }
        return false;
    }

    private static boolean isConstantCondition(@NotNull Pair<Pair<Variable, PsiElement>, Boolean> current, Pair<Pair<Variable, PsiElement>, Boolean> next) {
        final Map<PsiElement, List<PsiElement>> groups = groupValues(current, next);
        if (!groups.isEmpty()) {
            final boolean result = groups.values().stream()
                    .anyMatch(l -> l.size() == 2 && (OpenapiEquivalenceUtil.areEqual(l.get(0), l.get(1)) || (!current.second && l.stream().allMatch(MultipleValuesEqualityInIfBodyStrategy::isValueType))));
            groups.values().forEach(List::clear);
            groups.clear();
            return result;
        }
        return false;
    }

    @Nullable
    private static List<BinaryExpression> extractBinariesForAnalysis(@NotNull Variable variable, @NotNull GroupStatement scope) {
        final String variableName               = variable.getName();
        final List<BinaryExpression> candidates = new ArrayList<>();
        for (final Variable candidate : PsiTreeUtil.findChildrenOfType(scope, Variable.class)) {
            if (variableName.equals(candidate.getName())) {
                final PsiElement parent = candidate.getParent();
                if (parent instanceof BinaryExpression) {
                    /* collect target binary expressions */
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operator   = binary.getOperationType();
                    if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opNOT_EQUAL) {
                        candidates.add(binary);
                    }
                } else if (parent instanceof AssignmentExpression) {
                    /* stop collecting on the very first assignment */
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() != candidate) {
                        break;
                    }
                }
            }
        }
        return candidates.isEmpty() ? null : candidates;
    }

    @Nullable
    private static Pair<Pair<Variable, PsiElement>, Boolean> extractBinaryRepresentation(@NotNull BinaryExpression source) {
        Pair<Pair<Variable, PsiElement>, Boolean> result = null;
        final IElementType operator      = source.getOperationType();
        if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opNOT_EQUAL) {
            final PsiElement valueProbe = ExpressionSemanticUtil.getExpressionTroughParenthesis(source.getRightOperand());
            final PsiElement container  = valueProbe != null && isValueType(valueProbe) ? ExpressionSemanticUtil.getExpressionTroughParenthesis(source.getLeftOperand()) : valueProbe;
            if (container instanceof Variable) {
                final PsiElement value = OpenapiElementsUtil.getSecondOperand(source, container);
                if (value != null && (value instanceof Variable || isValueType(value))) {
                    result = new Pair<>(new Pair<>((Variable) container, value), operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opNOT_EQUAL);
                }
            }
        }
        return result;
    }

    @NotNull
    private static Map<PsiElement, List<PsiElement>> groupValues(@NotNull Pair<Pair<Variable, PsiElement>, Boolean> current, Pair<Pair<Variable, PsiElement>, Boolean> next) {
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

    private static boolean isValueType(@NotNull PsiElement element) {
        return  element instanceof StringLiteralExpression ||
                element instanceof ConstantReference ||
                element instanceof ClassConstantReference ||
                element instanceof ArrayCreationExpression ||
                OpenapiTypesUtil.isNumber(element);
    }

    @NotNull
    private static List<BinaryExpression> extractFragments(@NotNull BinaryExpression binary) {
        /* extract only binary expressions, ignore other condition parts */
        final List<BinaryExpression> result = new ArrayList<>();
        final IElementType operator         = binary.getOperationType();
        if (operator == PhpTokenTypes.opAND) {
            Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                    .map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                    .forEach(expression -> {
                        if (expression instanceof BinaryExpression) {
                            result.addAll(extractFragments((BinaryExpression) expression));
                        }
                    });
        } else {
            result.add(binary);
        }
        return result;
    }
}
