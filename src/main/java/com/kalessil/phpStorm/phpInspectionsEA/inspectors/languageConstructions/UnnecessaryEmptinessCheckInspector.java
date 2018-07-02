package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    private static final String messageControvertialIsset = "Doesn't match to previous isset-alike handling (perhaps always false when reached).";
    private static final String messageControvertialFalsy = "Doesn't match to previous falsy value handling (perhaps always false when reached).";
    private static final String messageControvertialNull  = "Doesn't match to previous null value handling (perhaps always false when reached).";
    private static final String messageNonContributing    = "Seems to be always true when reached.";
    private static final String messageNotEmpty           = "'isset(...) && ...' here can be replaced with '!empty(...)'.";
    private static final String messageEmpty              = "'!isset(...) || !...' here can be replaced with 'empty(...)'.";
    private static final String messageNotIsset           = "'empty(...) && ... === null' here can be replaced with '!isset(...)'.";
    private static final String messageIsset              = "!empty(...) || ... !== null' here can be replaced with 'isset(...)'.";

    // Inspection options.
    public boolean SUGGEST_SIMPLIFICATIONS  = true;
    public boolean REPORT_CONTROVERTIAL     = true;
    public boolean REPORT_NON_CONTRIBUTIONG = true;

    private static int STATE_DEFINED     = 1;
    private static int STATE_IS_NULL     = 2;
    private static int STATE_NOT_NULL    = 4;
    private static int STATE_IS_FALSY    = 8;
    private static int STATE_NOT_FALSY   = 16;

    private static int STATE_IS_SET      = 32;
    private static int STATE_NOT_SET     = 64;

    private static int STATE_CONFLICTING_IS_NULL  = STATE_IS_NULL | STATE_NOT_NULL;
    private static int STATE_CONFLICTING_IS_FALSY = STATE_IS_FALSY | STATE_NOT_FALSY;
    private static int STATE_CONFLICTING_IS_SET   = STATE_IS_SET | STATE_NOT_SET;

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
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final IElementType operator = expression.getOperationType();
                if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
                    /* false-positives: part of another condition */
                    final PsiElement parent  = expression.getParent();
                    final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
                    if (context instanceof BinaryExpression && ((BinaryExpression) context).getOperationType() == operator) {
                        return;
                    }

                    final HashMap<PsiElement, List<PsiElement>> grouping = this.group(this.extract(expression, operator));
                    grouping.forEach((argument, contexts) -> {
                        final int contextsCount = contexts.size();
                        if (contextsCount > 1) {
                            final boolean isTarget = contexts.stream().anyMatch(
                                e -> e instanceof PhpIsset || e instanceof PhpEmpty || e instanceof BinaryExpression
                            );
                            if (isTarget) {
                                int accumulatedState = this.calculateState(contexts.get(0));
                                for (int index = 1; index < contextsCount; ++index) {
                                    final PsiElement target = contexts.get(index);
                                    final int stateChange   = this.calculateState(target);
                                    final int newState      = accumulatedState | stateChange;
                                    if (accumulatedState == newState) {
                                        if (REPORT_NON_CONTRIBUTIONG) {
                                            holder.registerProblem(this.target(target, argument), messageNonContributing);
                                        }
                                    } else if ((newState & STATE_CONFLICTING_IS_NULL) == STATE_CONFLICTING_IS_NULL) {
                                        if (REPORT_CONTROVERTIAL) {
                                            holder.registerProblem(this.target(target, argument), messageControvertialNull);
                                        }
                                    } else if ((newState & STATE_CONFLICTING_IS_FALSY) == STATE_CONFLICTING_IS_FALSY) {
                                        if (REPORT_CONTROVERTIAL) {
                                            holder.registerProblem(this.target(target, argument), messageControvertialFalsy);
                                        }
                                    } else if ((newState & STATE_CONFLICTING_IS_SET) == STATE_CONFLICTING_IS_SET) {
                                        if (REPORT_CONTROVERTIAL) {
                                            holder.registerProblem(this.target(target, argument), messageControvertialIsset);
                                        }
                                    }
                                    accumulatedState = newState;
                                }

                                if (SUGGEST_SIMPLIFICATIONS) {
                                    if (contexts.stream().noneMatch(e -> e instanceof PhpEmpty)) {
                                        final Optional<PsiElement> isset
                                                = contexts.stream().filter(e -> e instanceof PhpIsset).findFirst();
                                        if (isset.isPresent()) {
                                            final Optional<PsiElement> candidate = contexts.stream()
                                                    .filter(e -> e.getClass() == argument.getClass()).findFirst();
                                            if (candidate.isPresent()) {
                                                if (operator == PhpTokenTypes.opAND) {
                                                    if (!this.isInverted(isset.get()) && !this.isInverted(candidate.get())) {
                                                        holder.registerProblem(this.target(isset.get(), argument), messageNotEmpty);
                                                    }
                                                } else {
                                                    if (this.isInverted(isset.get()) && this.isInverted(candidate.get())) {
                                                        holder.registerProblem(this.target(isset.get(), argument), messageEmpty);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        final Optional<PsiElement> empty
                                                = contexts.stream().filter(e -> e instanceof PhpEmpty).findFirst();
                                        if (empty.isPresent()) {
                                            IElementType targetOperator = null;
                                            String targetMessage        = null;
                                            if (operator == PhpTokenTypes.opAND && !this.isInverted(empty.get())) {
                                                targetOperator = PhpTokenTypes.opIDENTICAL;
                                                targetMessage  = messageNotIsset;
                                            } else if (operator == PhpTokenTypes.opOR && this.isInverted(empty.get())) {
                                                targetOperator = PhpTokenTypes.opNOT_IDENTICAL;
                                                targetMessage  = messageIsset;
                                            }
                                            if (targetOperator != null) {
                                                for (final PsiElement target : contexts) {
                                                    if (
                                                        target instanceof BinaryExpression &&
                                                        ((BinaryExpression) target).getOperationType() == targetOperator
                                                    ) {
                                                        holder.registerProblem(this.target(empty.get(), argument), targetMessage);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        contexts.clear();
                    });
                    grouping.clear();
                }
            }

            private PsiElement target(@NotNull PsiElement expression, @NotNull PsiElement subject) {
                PsiElement result = expression;
                if (expression instanceof PhpIsset) {
                    final PsiElement[] arguments = ((PhpIsset) expression).getVariables();
                    if (arguments.length > 1) {
                        final Optional<PsiElement> match = Arrays.stream(arguments)
                                .filter(argument -> OpenapiEquivalenceUtil.areEqual(subject, argument))
                                .findFirst();
                        if (match.isPresent()) {
                            result = match.get();
                        }
                    }
                }
                return result;
            }

            private boolean isInverted (@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                return (parent instanceof ParenthesizedExpression ? parent.getParent() : parent) instanceof UnaryExpression;
            }

            private int calculateState(@NotNull PsiElement expression) {
                final int result;
                final boolean isInverted = this.isInverted(expression);
                if (expression instanceof PhpEmpty) {
                    result = isInverted
                            ? (STATE_IS_SET | STATE_DEFINED | STATE_NOT_FALSY)
                            : (STATE_NOT_SET | STATE_DEFINED | STATE_IS_FALSY);
                } else if (expression instanceof PhpIsset) {
                    result = isInverted
                            ? (STATE_NOT_SET | STATE_DEFINED | STATE_IS_NULL)
                            : (STATE_IS_SET | STATE_DEFINED | STATE_NOT_NULL);
                } else if (expression instanceof BinaryExpression) {
                    final IElementType operation = ((BinaryExpression) expression).getOperationType();
                    if (operation == PhpTokenTypes.opIDENTICAL) {
                        result = STATE_DEFINED | STATE_IS_NULL;
                    } else if (operation == PhpTokenTypes.opNOT_IDENTICAL) {
                        result = STATE_DEFINED | STATE_NOT_NULL;
                    } else if (operation == PhpTokenTypes.opEQUAL) {
                        result = STATE_DEFINED | STATE_IS_FALSY | STATE_IS_NULL;
                    } else if (operation == PhpTokenTypes.opNOT_EQUAL) {
                        result = STATE_DEFINED | STATE_NOT_FALSY | STATE_NOT_NULL;
                    } else {
                        result = STATE_DEFINED;
                    }
                } else {
                    result = isInverted ? (STATE_DEFINED | STATE_IS_FALSY) : (STATE_DEFINED | STATE_NOT_FALSY);
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
                            final Optional<PsiElement> key = result.keySet().stream()
                                    .filter(e -> e != null && argument != null && OpenapiEquivalenceUtil.areEqual(e, argument))
                                    .findFirst();
                            if (!key.isPresent()) {
                                result.put(argument, new ArrayList<>(Collections.singletonList(expression)));
                            } else {
                                result.get(key.get()).add(expression);
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

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Suggest simplifications", SUGGEST_SIMPLIFICATIONS, (isSelected) -> SUGGEST_SIMPLIFICATIONS = isSelected);
            component.addCheckbox("Report ambiguous statements", REPORT_NON_CONTRIBUTIONG, (isSelected) -> REPORT_NON_CONTRIBUTIONG = isSelected);
            component.addCheckbox("Report controversial statements", REPORT_CONTROVERTIAL, (isSelected) -> REPORT_CONTROVERTIAL = isSelected);
        });
    }

}
