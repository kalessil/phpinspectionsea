package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class UnnecessaryEmptinessCheckInspector extends PhpInspection {
    private static final String messageControversialIsset    = "Doesn't match to previous isset-alike handling (perhaps always false when reached).";
    private static final String messageControversialFalsy    = "Doesn't match to previous falsy value handling (perhaps always false when reached).";
    private static final String messageControversialNull     = "Doesn't match to previous null value handling (perhaps always false when reached).";
    private static final String messageAlwaysTrue            = "Seems to be always true when reached.";
    private static final String messageAlwaysFalse           = "Seems to be always false when reached.";
    private static final String messageIssetCanBeDropped     = "Perhaps can be dropped, as it covered by a following 'empty(...)'.";
    private static final String messageNotEmpty              = "'isset(...) && ...' here can be replaced with '!empty(%s)' (simplification).";
    private static final String messageEmpty                 = "'!isset(...) || !...' here can be replaced with 'empty(%s)' (simplification).";
    private static final String messageNotIsset              = "'empty(...) && ... === null' here can be replaced with '!isset(%s)' (simplification).";
    private static final String messageIsset                 = "'!empty(...) || ... !== null' here can be replaced with 'isset(%s)' (simplification).";
    private static final String messageNotEmptyArrayCount    = "'is_array(...) && count(...)' here probably can be replaced with '%s && is_array(%s)' (simplification).";
    private static final String messageEmptyArrayCount       = "'is_array(...) && !count(...)' here probably can be replaced with '!%s && is_array(%s)' (simplification).";
    private static final String messageUseCoalescing         = "'%s' can be used instead (simplification, reduces cognitive load).";

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
    @Override
    public String getShortName() {
        return "UnnecessaryEmptinessCheckInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unnecessary emptiness check";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpIsset(@NotNull PhpIsset isset) {
                if (this.shouldSkipAnalysis(isset, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

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
                        /* since alternative is known, we did hit the pattern */
                        if (alternative != null && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP700)) {
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

            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

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
                                    e instanceof BinaryExpression ||
                                    e instanceof FunctionReference
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
                                        this.analyzeForDroppingIsset(argument, contexts, operator, reported);
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
                                                /* false-positives: nested function calls in binary operations */
                                                final boolean report = PsiTreeUtil.findChildrenOfType(target, argument.getClass()).stream()
                                                        .filter(element -> OpenapiEquivalenceUtil.areEqual(element, argument))
                                                        .noneMatch(element -> element.getParent() instanceof ParameterList);
                                                if (report) {
                                                    final PsiElement node = this.target(target, argument);
                                                    if (reported.add(node)) {
                                                        holder.registerProblem(node, messageAlwaysTrue);
                                                    }
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
                    if (!first.isPresent()) {
                        for (final PsiElement expression : contexts) {
                            if (expression instanceof BinaryExpression) {
                                /* is_array(...) && count() ([!]==[=]|>) 0 */
                                final BinaryExpression binary = (BinaryExpression) expression;
                                final PsiElement right        = binary.getRightOperand();
                                final PsiElement left         = binary.getLeftOperand();
                                if (OpenapiTypesUtil.isFunctionReference(left) && OpenapiTypesUtil.isNumber(right)) {
                                    final boolean isZero = right != null && right.getText().equals("0");
                                    if (isZero) {
                                        final FunctionReference call = (FunctionReference) left;
                                        final String functionName    = call.getName();
                                        if (functionName != null && (functionName.equals("count") || functionName.equals("sizeof"))) {
                                            final IElementType operator = binary.getOperationType();
                                            if (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opIDENTICAL) {
                                                if (reported.add(binary)) {
                                                    holder.registerProblem(binary, String.format(messageEmptyArrayCount, argument.getText(), argument.getText()));
                                                }
                                            } else if (operator == PhpTokenTypes.opNOT_EQUAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                                                if (reported.add(binary)) {
                                                    holder.registerProblem(binary, String.format(messageNotEmptyArrayCount, argument.getText(), argument.getText()));
                                                }
                                            } else if (operator == PhpTokenTypes.opGREATER) {
                                                if (reported.add(binary)) {
                                                    holder.registerProblem(binary, String.format(messageNotEmptyArrayCount, argument.getText(), argument.getText()));
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            } else if (OpenapiTypesUtil.isFunctionReference(expression)) {
                                /* is_array(...) && [!]count() */
                                final FunctionReference call = (FunctionReference) expression;
                                final String functionName    = call.getName();
                                if (functionName != null && (functionName.equals("count") || functionName.equals("sizeof"))) {
                                    if (reported.add(call)) {
                                        final String message = this.isInverted(call) ? messageEmptyArrayCount : messageNotEmptyArrayCount;
                                        holder.registerProblem(call, String.format(message, argument.getText(), argument.getText()));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            private void analyzeForDroppingIsset(
                    @NotNull PsiElement argument,
                    @NotNull List<PsiElement> contexts,
                    @NotNull IElementType operator,
                    @NotNull Set<PsiElement> reported
            ) {
                final Optional<PsiElement> empty = contexts.stream().filter(e -> e instanceof PhpEmpty).findFirst();
                if (empty.isPresent()) {
                    final Optional<PsiElement> isset = contexts.stream().filter(e -> e instanceof PhpIsset).findFirst();
                    if (isset.isPresent()) {
                        if (operator == PhpTokenTypes.opAND) {
                            final boolean isEmptyInverted = this.isInverted(empty.get());
                            final boolean isIssetInverted = this.isInverted(isset.get());
                            if (isIssetInverted && !isEmptyInverted) {
                                /* !isset($request) && empty($request): empty() is always true */
                                final PsiElement node = this.target(empty.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageAlwaysTrue);
                                }
                            } else if (!isIssetInverted && isEmptyInverted) {
                                /* isset($request) && !empty($request): isset() can be dropped  */
                                final PsiElement node = this.target(isset.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageIssetCanBeDropped);
                                }
                            }
                        } else if (operator == PhpTokenTypes.opOR) {
                            final boolean isEmptyInverted = this.isInverted(empty.get());
                            final boolean isIssetInverted = this.isInverted(isset.get());
                            if (isIssetInverted && !isEmptyInverted) {
                                /* !isset($request) || empty($request): !isset() can be dropped */
                                final PsiElement node = this.target(isset.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageIssetCanBeDropped);
                                }
                            } else if (!isIssetInverted && isEmptyInverted) {
                                /* isset($request) || !empty($request): !empty() is always false */
                                final PsiElement node = this.target(empty.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, messageAlwaysFalse);
                                }
                            }
                        }
                    }
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
                        targetMessage  = String.format(messageNotIsset, argument.getText());
                    } else if (operator == PhpTokenTypes.opOR && this.isInverted(empty.get())) {
                        targetOperator = PhpTokenTypes.opNOT_IDENTICAL;
                        targetMessage  = String.format(messageIsset, argument.getText());
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
                                    holder.registerProblem(node, String.format(messageNotEmpty, argument.getText()));
                                }
                            }
                        } else {
                            if (this.isInverted(isset.get()) && this.isInverted(candidate.get())) {
                                final PsiElement node = this.target(isset.get(), argument);
                                if (reported.add(node)) {
                                    holder.registerProblem(node, String.format(messageEmpty, argument.getText()));
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
                } else if (expression instanceof FunctionReference) {
                    result = 0;
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
                        final IElementType operator   = binary.getOperationType();
                        if (
                            OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator) ||
                            operator == PhpTokenTypes.opGREATER
                        ) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (right != null && left != null) {
                                if (PhpLanguageUtil.isNull(left)) {
                                    arguments = new PsiElement[]{right};
                                } else if (PhpLanguageUtil.isNull(right)) {
                                    arguments = new PsiElement[]{left};
                                } else if (OpenapiTypesUtil.isNumber(right) && OpenapiTypesUtil.isFunctionReference(left)) {
                                    final FunctionReference reference = (FunctionReference) left;
                                    arguments = Arrays.stream(reference.getParameters())
                                            .filter(a -> a instanceof Variable)
                                            .toArray(PsiElement[]::new);
                                }
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
                        .map(ExpressionSemanticUtil::getExpressionTroughParenthesis).filter(Objects::nonNull)
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
