package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
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

    private static int STATE_DEFINED   = 1;
    private static int STATE_NOT_NULL  = 2;
    private static int STATE_NOT_FALCY = 4;

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
                            final boolean isTarget = contexts.stream().anyMatch(e -> e instanceof PhpEmpty || e instanceof PhpIsset);
                            if (isTarget) {

                                // isset(...) && ...           => !empty(...) + anomalies
                                // !isset(...) || !...         => empty(...)  + anomalies
                                // isset(...) && ... !== null  => isset(...)  + anomalies
                                // !isset(...) || ... === null => !isset(...) + anomalies
                                // isset(...) && !empty(...)   => isset(...)  + anomalies
                                // !isset(...) || empty(...)   => !isset(...)  + anomalies
                                // empty(...) && !...          => empty(...)  + anomalies
                                // !empty(...) || ...          => !empty(...)  + anomalies
                                // empty(...) && ... === null  => empty(...)  + anomalies
                                // !empty(...) || ... !== null => !empty(...) + anomalies
                                // empty(...) && !isset(...)   => empty(...)  + anomalies
                                // !empty(...) || isset(...)   => !empty(...)  + anomalies

                                holder.registerProblem(argument, contexts.toString());
                            }
                        }
                        contexts.clear();
                    });
                    grouping.clear();
                }
            }

            private int calculateState(@NotNull PsiElement expression) {
                final int result;
                final boolean isInverted = expression.getParent() instanceof UnaryExpression;
                if (expression instanceof PhpEmpty) {
                    result = isInverted ? (STATE_DEFINED & STATE_NOT_FALCY & STATE_NOT_NULL) : (~STATE_DEFINED | ~STATE_NOT_FALCY | ~STATE_NOT_NULL);
                } else if (expression instanceof PhpIsset) {
                    result =
                } else if (expression instanceof BinaryExpression) {

                } else {
                    result = STATE_DEFINED & (isInverted ? (~STATE_NOT_FALCY | ~STATE_NOT_NULL) : (STATE_NOT_FALCY & STATE_NOT_NULL));
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
