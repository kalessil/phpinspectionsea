package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MultipleFalsyValuesCheckStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true when reached.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false when reached.";

    private static boolean apply(
            @NotNull PsiElement target,
            @NotNull PsiElement subject,
            @NotNull IElementType operator,
            boolean isFalsyExpected,
            @NotNull Map<PsiElement, Boolean> falsyStates,
            @NotNull ProblemsHolder holder
    ) {
        boolean result = false;
        final Optional<PsiElement> matched = falsyStates.keySet().stream()
                .filter(key -> OpenapiEquivalenceUtil.areEqual(key, subject))
                .findFirst();
        if (matched.isPresent()) {
            result = true;
            final boolean isFalsySame = isFalsyExpected == falsyStates.get(matched.get());
            if (operator == PhpTokenTypes.opAND) {
                holder.registerProblem(
                        target,
                        String.format(ReportingUtil.wrapReportedMessage(isFalsySame ? messageAlwaysTrue : messageAlwaysFalse), target.getText()),
                        isFalsySame ? ProblemHighlightType.LIKE_UNUSED_SYMBOL : ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );
            } else {
                holder.registerProblem(
                        target,
                        String.format(ReportingUtil.wrapReportedMessage(isFalsySame ? messageAlwaysFalse : messageAlwaysTrue), target.getText()),
                        isFalsySame ? ProblemHighlightType.LIKE_UNUSED_SYMBOL : ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );
            }
        } else {
            falsyStates.put(subject, isFalsyExpected);
        }
        return result;
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator != null && (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR)) {
            /* false-positives: part of another condition */
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<PsiElement> fragments = extractFragments(expression, operator);
                if (fragments.size() > 1) {
                    final Map<PsiElement, Boolean> falsyStates = new HashMap<>();
                    for (final PsiElement fragment : fragments) {
                        if (fragment instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) fragment;
                            final PsiElement right        = binary.getRightOperand();
                            if (right != null) {
                                final PsiElement subject = PhpLanguageUtil.isFalsyValue(right) ? binary.getLeftOperand() : right;
                                if (subject != null) {
                                    final boolean isFalsyExpected = binary.getOperationType() == PhpTokenTypes.opEQUAL;
                                    if (apply(binary, subject, operator, isFalsyExpected, falsyStates, holder)) {
                                        result = true;
                                    }
                                }
                            }
                        } else if (fragment instanceof Variable) {
                            final Variable variable = (Variable) fragment;
                            if (apply(variable, variable, operator, false, falsyStates, holder)) {
                                result = true;
                            }
                        } else if (fragment instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) fragment;
                            final PsiElement argument   = unary.getValue();
                            if (argument instanceof Variable && apply(unary, argument, operator, true, falsyStates, holder)) {
                                result = true;
                            }
                        }
                    }
                    falsyStates.clear();
                }
                fragments.clear();
            }
        }
        return result;
    }

    @NotNull
    private static List<PsiElement> extractFragments(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
        final List<PsiElement> result     = new ArrayList<>();
        final IElementType binaryOperator = binary.getOperationType();
        if (binaryOperator == operator) {
            Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                    .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                    .forEach(expression -> {
                        if (expression instanceof BinaryExpression) {
                            result.addAll(extractFragments((BinaryExpression) expression, operator));
                        } else if (expression instanceof Variable) {
                            result.add(expression);
                        } else if (expression instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) expression;
                            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                final PsiElement argument = unary.getValue();
                                if (argument instanceof Variable) {
                                    result.add(unary);
                                }
                            }
                        }
                    });
        } else if (binaryOperator == PhpTokenTypes.opEQUAL || binaryOperator == PhpTokenTypes.opNOT_EQUAL) {
            final boolean isFalsyCheck = Stream.of(binary.getRightOperand(), binary.getLeftOperand())
                    .filter(Objects::nonNull).anyMatch(PhpLanguageUtil::isFalsyValue);
            if (isFalsyCheck) {
                result.add(binary);
            }
        }
        return result;
    }
}
