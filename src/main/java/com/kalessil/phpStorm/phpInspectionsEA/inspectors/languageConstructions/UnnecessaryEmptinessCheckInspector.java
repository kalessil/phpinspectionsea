package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
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

public class UnnecessaryEmptinessCheckInspector extends BasePhpInspection {
    private static final String message = "...";

    private static int STATE_DEFINED     = 1;
    private static int STATE_NOT_DEFINED = 2;
    private static int STATE_IS_NULL     = 4;
    private static int STATE_NOT_NULL    = 8;
    private static int STATE_IS_FALSY    = 16;
    private static int STATE_NOT_FALSY   = 32;

    @NotNull
    public String getShortName() {
        return "UnnecessaryEmptinessCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operator = expression.getOperationType();
                if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opAND) {
                    /* false-positives: part of another condition */
                    final PsiElement parent = expression.getParent();
                    if (parent instanceof BinaryExpression && ((BinaryExpression) parent).getOperationType() == operator) {
                        return;
                    }

                    final HashMap<PsiElement, List<PsiElement>> grouping = this.group(this.extract(expression, operator));
                    grouping.forEach((argument, contexts) -> {
                        if (contexts.size() > 1) {
                            final boolean isTarget = contexts.stream().anyMatch(
                                e -> e instanceof PhpIsset || e instanceof PhpEmpty || e instanceof BinaryExpression
                            );
                            if (!isTarget) {
                                contexts.clear();
                                return;
                            }

                            int accumulatedState = calculateState(contexts.get(0));
                            for (int index = 1, max = contexts.size(); index < max; ++index) {
                                final int stateChange = calculateState(contexts.get(index));
//holder.registerProblem(contexts.get(index), String.format("%s <- %s", accumulatedState, stateChange));

                                /* accumulatedState [&, |] stateChange suppose to make SOME difference (always true case) */
                                final int newState = operator == PhpTokenTypes.opAND
                                        ? (accumulatedState & stateChange)
                                        : (accumulatedState | stateChange);
                                if (accumulatedState == newState) {
                                    holder.registerProblem(contexts.get(index), "Seems to be always true.");
                                    contexts.clear();
                                    return;
                                }

                                /* controversial states resolution */
                                if ((newState & (STATE_IS_NULL | STATE_NOT_NULL | STATE_IS_FALSY | STATE_NOT_FALSY)) > 0) {
                                    holder.registerProblem(contexts.get(index), "Seems to be always false.");
                                    contexts.clear();
                                    return;
                                }

                                accumulatedState = newState;
                            }
                        }
                        contexts.clear();
                    });
                    grouping.clear();
                }
            }

            private int calculateState(@NotNull PsiElement expression) {
                PsiElement parent = expression.getParent();
                parent            = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;

                final int result;
                final boolean isInverted = parent instanceof UnaryExpression;
                if (expression instanceof PhpEmpty) {
                    result = isInverted
                            ? (STATE_DEFINED | STATE_NOT_FALSY | STATE_NOT_NULL)
                            : (STATE_NOT_DEFINED | STATE_IS_FALSY | STATE_IS_NULL);
                } else if (expression instanceof PhpIsset) {
                    result = isInverted
                            ? (STATE_NOT_DEFINED | STATE_IS_NULL)
                            : (STATE_DEFINED | STATE_NOT_NULL | STATE_IS_FALSY);
                } else if (expression instanceof BinaryExpression) {
                    final IElementType operation = ((BinaryExpression) expression).getOperationType();
                    if (operation == PhpTokenTypes.opIDENTICAL) {
                        result = STATE_DEFINED | STATE_NOT_FALSY | STATE_IS_NULL;
                    } else if (operation == PhpTokenTypes.opNOT_IDENTICAL) {
                        result = STATE_DEFINED | STATE_IS_FALSY | STATE_NOT_NULL;
                    } else if (operation == PhpTokenTypes.opEQUAL) {
                        result = STATE_DEFINED | STATE_IS_FALSY | STATE_IS_NULL;
                    } else if (operation == PhpTokenTypes.opNOT_EQUAL) {
                        result = STATE_DEFINED | STATE_NOT_FALSY | STATE_NOT_NULL;
                    } else {
                        result = STATE_DEFINED;
                    }
                } else {
                    result = STATE_DEFINED | (isInverted ? (STATE_IS_FALSY | STATE_IS_NULL) : (STATE_NOT_FALSY | STATE_NOT_NULL));
                }
                return result;
            }

            @NotNull
            private HashMap<PsiElement, List<PsiElement>> group(@NotNull List<PsiElement> conditions) {
                final HashMap<PsiElement, List<PsiElement>> result = new HashMap<>();
                for (final PsiElement expression : conditions) {
                    PsiElement[] arguments = null;

                    /* prepare expression to contexts mapping */
                    if (expression instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) expression;
                        if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binary.getOperationType())) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (right != null && PhpLanguageUtil.isNull(left)) {
                                arguments = new PsiElement[]{right};
                            } else if (left != null && PhpLanguageUtil.isNull(right)) {
                                arguments = new PsiElement[]{left};
                            }
                        }
                    } else if (expression instanceof PhpEmpty) {
                        arguments = ((PhpEmpty) expression).getVariables();
                    } else if (expression instanceof PhpIsset) {
                        arguments = ((PhpIsset) expression).getVariables();
                    } else {
                        arguments = new PsiElement[]{expression};
                    }

                    /* perform expression to contexts mapping */
                    if (arguments != null && arguments.length > 0) {
                        for (final PsiElement argument : arguments) {
                            final PsiElement context       = arguments.length == 1 ? expression : argument;
                            final Optional<PsiElement> key = result.keySet().stream()
                                    .filter(element -> OpeanapiEquivalenceUtil.areEqual(element, argument)).findFirst();
                            if (!key.isPresent()) {
                                result.put(argument, new ArrayList<>(Collections.singletonList(context)));
                            } else {
                                result.get(key.get()).add(context);
                            }
                        }
                    }
                }
                conditions.clear();
                return result;
            }

            @NotNull
            private List<PsiElement> extract(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
                final List<PsiElement> result = new ArrayList<>();
                if (binary.getOperationType() == operator) {
                    Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                        .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                        .forEach(expression -> {
                            if (expression instanceof BinaryExpression) {
                                result.addAll(extract((BinaryExpression) expression, operator));
                            } else if (expression instanceof UnaryExpression) {
                                final UnaryExpression unary = (UnaryExpression) expression;
                                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                    result.add(ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue()));
                                } else {
                                    result.add(unary);
                                }
                            } else {
                                result.add(expression);
                            }
                        });
                } else {
                    result.add(binary);
                }
                return result;
            }
        };
    }
}
