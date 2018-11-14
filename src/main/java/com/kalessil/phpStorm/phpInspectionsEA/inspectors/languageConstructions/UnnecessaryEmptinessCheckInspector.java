package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
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
    private static final String messageControversialIsset    = "Doesn't match to previous isset-alike handling (perhaps always false when reached).";
    private static final String messageControversialFalsy    = "Doesn't match to previous falsy value handling (perhaps always false when reached).";
    private static final String messageControversialNull     = "Doesn't match to previous null value handling (perhaps always false when reached).";
    private static final String messageNonContributing       = "Seems to be always true when reached.";
    private static final String messageNotEmpty              = "'isset(...) && ...' here can be replaced with '!empty(...)'.";
    private static final String messageEmpty                 = "'!isset(...) || !...' here can be replaced with 'empty(...)'.";
    private static final String messageNotIsset              = "'empty(...) && ... === null' here can be replaced with '!isset(...)'.";
    private static final String messageIsset                 = "'!empty(...) || ... !== null' here can be replaced with 'isset(...)'.";
    private static final String messageNotEmptyArrayImplicit = "'is_array(...) && !empty(...)' here can be replaced with '... !== []'.";
    private static final String messageEmptyArrayImplicit    = "'is_array(...) && empty(...)' here can be replaced with '... === []'.";
    private static final String messageNotEmptyArrayIndirect = "'is_array(...) && ...' here can be replaced with '... !== []'.";
    private static final String messageEmptyArrayIndirect    = "'is_array(...) && !...' here can be replaced with '... === []'.";
    private static final String messageUseCoalescing         = "'%s' can be used instead (reduces cognitive load).";

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

    private static int STATE_CONFLICTING_IS_NULL  = STATE_IS_NULL  | STATE_NOT_NULL;
    private static int STATE_CONFLICTING_IS_FALSY = STATE_IS_FALSY | STATE_NOT_FALSY;
    private static int STATE_CONFLICTING_IS_SET   = STATE_IS_SET   | STATE_NOT_SET;

    @NotNull
    public String getShortName() {
        return "UnnecessaryEmptinessCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* [!]empty($argument) -> [!]$argument: do not report as it add much warnings is typical projects */

            @Override
            public void visitPhpIsset(@NotNull PhpIsset isset) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(isset))                  { return; }

                if (SUGGEST_SIMPLIFICATIONS) {
                    final PsiElement[] arguments = isset.getVariables();
                    if (arguments.length == 1) {
                        PsiElement alternative  = null;
                        final PsiElement parent = isset.getParent();
                        if (parent instanceof TernaryExpression) {
                            final TernaryExpression ternary = (TernaryExpression) parent;
                            if (ternary.isShort() && ternary.getCondition() == isset) {
                                alternative = ternary.getFalseVariant();
                            }
                        } else if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            if (binary.getLeftOperand() == isset && binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                                alternative = binary.getRightOperand();
                            }
                        }
                        /* since alternative is known, we did hiy yhe pattern */
                        if (alternative != null) {
                            final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                            if (php.compareTo(PhpLanguageLevel.PHP700) >= 0) {
                                final String replacement = String.format("%s ?? %s", arguments[0].getText(), alternative.getText());
                                holder.registerProblem(
                                        parent,
                                        String.format(messageUseCoalescing, replacement),
                                        ProblemHighlightType.WEAK_WARNING
                                );
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

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
                                    e -> e instanceof PhpIsset ||
                                    e instanceof PhpEmpty ||
                                    e instanceof Variable ||
                                    e instanceof BinaryExpression
                            );
                            if (isTarget) {
                                final Set<PsiElement> reported = new HashSet<>();
                                /* firstly suggest simplifications */
                                if (SUGGEST_SIMPLIFICATIONS) {
                                    if (operator == PhpTokenTypes.opAND) {
                                        this.analyzeForUsingEmptyArrayComparison(argument, contexts, reported);
                                    }

                                    if (contexts.stream().noneMatch(e -> e instanceof PhpEmpty)) {
                                        this.analyzeForUsingEmpty(argument, contexts, operator, reported);
                                    } else {
                                        this.analyzeForUsingIsset(argument, contexts, operator, reported);
                                    }
                                }

                                /* secondly report issues */
                                int accumulatedState = this.calculateState(contexts.get(0));
                                for (int index = 1; index < contextsCount; ++index) {
                                    final PsiElement target = contexts.get(index);
                                    if (!OpenapiTypesUtil.isFunctionReference(target)) {
                                        final int stateChange   = this.calculateState(target);
                                        final int newState      = accumulatedState | stateChange;
                                        if (accumulatedState == newState) {
                                            if (REPORT_NON_CONTRIBUTIONG) {
                                                final PsiElement node = this.target(target, argument);
                                                if (reported.add(node)) {
                                                    holder.registerProblem(node, messageNonContributing);
                                                }
                                            }
                                        } else if ((newState & STATE_CONFLICTING_IS_NULL) == STATE_CONFLICTING_IS_NULL) {
                                            if (REPORT_CONTROVERTIAL) {
                                                final PsiElement node = this.target(target, argument);
                                                if (reported.add(node)) {
                                                    holder.registerProblem(node, messageControversialNull);
                                                }
                                            }
                                        } else if ((newState & STATE_CONFLICTING_IS_FALSY) == STATE_CONFLICTING_IS_FALSY) {
                                            if (REPORT_CONTROVERTIAL) {
                                                final PsiElement node = this.target(target, argument);
                                                if (reported.add(node)) {
                                                    holder.registerProblem(node, messageControversialFalsy);
                                                }
                                            }
                                        } else if ((newState & STATE_CONFLICTING_IS_SET) == STATE_CONFLICTING_IS_SET) {
                                            if (REPORT_CONTROVERTIAL) {
                                                final PsiElement node = this.target(target, argument);
                                                if (reported.add(node)) {
                                                    holder.registerProblem(node, messageControversialIsset);
                                                }
                                            }
                                        }
                                        accumulatedState = newState;
                                    }
                                }
                                reported.clear();
                            }
                        }
                        contexts.clear();
                    });
                    grouping.clear();
                }
            }

            private void analyzeForUsingEmptyArrayComparison(
                    @NotNull PsiElement argument,
                    @NotNull List<PsiElement> contexts,
                    @NotNull Set<PsiElement> reported
            ) {
                final Optional<PsiElement> reference = contexts.stream()
                        .filter(e -> e instanceof FunctionReference)
                        .filter(e -> "is_array".equals(((FunctionReference) e).getName()))
                        .findFirst();
                if (reference.isPresent() && !this.isInverted(reference.get())) {
                    final Optional<PsiElement> first = contexts.stream()
                            .filter(e -> e instanceof PhpEmpty || e instanceof Variable)
                            .findFirst();
                    if (first.isPresent()) {
                        final PsiElement expression = first.get();
                        final PsiElement node       = this.target(expression, argument);
                        if (reported.add(node)) {
                            /* is_array(...) && [!]empty(...) */
                            if (expression instanceof PhpEmpty) {
                                final String message = this.isInverted(expression) ? messageNotEmptyArrayImplicit : messageEmptyArrayImplicit;
                                holder.registerProblem(node, message);
                            } else if (expression instanceof Variable) {
                                /* is_array($...) && [!]$... */
                                final String message = this.isInverted(expression) ? messageEmptyArrayIndirect : messageNotEmptyArrayIndirect;
                                holder.registerProblem(node, message);
                            }
                        }
                    }
                    // TODO: count(), !count(), count() ==[=] 0, count() !=[=] 0, count() > 0
                }
            }

            private void analyzeForUsingIsset(
                    @NotNull PsiElement argument,
                    @NotNull List<PsiElement> contexts,
                    @NotNull IElementType operator,
                    @NotNull Set<PsiElement> reported
            ) {
                final Optional<PsiElement> empty = contexts.stream().filter(e -> e instanceof PhpEmpty).findFirst();
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
                            if (target instanceof BinaryExpression) {
                                final IElementType operation = ((BinaryExpression) target).getOperationType();
                                if (operation == targetOperator) {
                                    final PsiElement node = this.target(empty.get(), argument);
                                    if (reported.add(node)) {
                                        holder.registerProblem(node, targetMessage);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            private void analyzeForUsingEmpty(
                    @NotNull PsiElement argument,
                    @NotNull List<PsiElement> contexts,
                    @NotNull IElementType operator,
                    @NotNull Set<PsiElement> reported
            ) {
                final Optional<PsiElement> isset = contexts.stream().filter(e -> e instanceof PhpIsset).findFirst();
                if (isset.isPresent()) {
                    final Optional<PsiElement> candidate = contexts.stream()
                            .filter(e -> e.getClass() == argument.getClass()).findFirst();
                    if (candidate.isPresent()) {
                        if (operator == PhpTokenTypes.opAND) {
                            if (!this.isInverted(isset.get()) && !this.isInverted(candidate.get())) {
                                final PsiElement node = this.target(isset.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageNotEmpty);
                                }
                            }
                        } else {
                            if (this.isInverted(isset.get()) && this.isInverted(candidate.get())) {
                                final PsiElement node = this.target(isset.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageEmpty);
                                }
                            }
                        }
                    }
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
                    } else if (OpenapiTypesUtil.isFunctionReference(expression)) {
                        final FunctionReference reference = (FunctionReference) expression;
                        arguments = Arrays.stream(reference.getParameters())
                                .filter(a -> a instanceof Variable)
                                .toArray(PsiElement[]::new);
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
                                result.addAll(this.extract((BinaryExpression) expression, operator));
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
