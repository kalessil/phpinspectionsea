package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class SuspiciousLoopInspector extends PhpInspection {
    private static final String messageMultipleConditions = "Please use && or || for multiple conditions. Currently no checks are performed after first positive result.";
    private static final String messageLoopBoundaries     = "Conditions and repeated operations are not complimentary, please check what's going on here.";
    private static final String patternOverridesLoopVars  = "Variable '$%s' is introduced in a outer loop and overridden here.";
    private static final String patternOverridesParameter = "Variable '$%s' is introduced as a %s parameter and overridden here.";
    private static final String patternConditionAnomaly   = "A parent condition '%s' looks suspicious.";
    private static final String patternEmptyArray         = "'%s' is probably an empty array.";

    private static final Map<IElementType, IElementType> operationsInversion = new HashMap<>();
    private static final Set<IElementType> operationsAnomaly                 = new HashSet<>();
    static {
        operationsInversion.put(PhpTokenTypes.opGREATER,          PhpTokenTypes.opLESS_OR_EQUAL);
        operationsInversion.put(PhpTokenTypes.opGREATER_OR_EQUAL, PhpTokenTypes.opLESS);
        operationsInversion.put(PhpTokenTypes.opLESS,             PhpTokenTypes.opGREATER_OR_EQUAL);
        operationsInversion.put(PhpTokenTypes.opLESS_OR_EQUAL,    PhpTokenTypes.opGREATER);

        operationsAnomaly.add(PhpTokenTypes.opLESS);
        operationsAnomaly.add(PhpTokenTypes.opLESS_OR_EQUAL);
        operationsAnomaly.add(PhpTokenTypes.opEQUAL);
        operationsAnomaly.add(PhpTokenTypes.opIDENTICAL);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious loop";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                this.inspectVariables(statement);
                this.inspectParentConditions(statement, statement.getArray());
                // this.inspectSource(statement, statement.getArray());
            }

            @Override
            public void visitPhpFor(@NotNull For statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                this.inspectConditions(statement);
                this.inspectVariables(statement);
                this.findUsedContainers(statement).forEach(container -> this.inspectParentConditions(statement, container));
                this.inspectBoundariesCorrectness(statement);
            }

            private void inspectSource(@NotNull ForeachStatement loop, @Nullable PsiElement source) {
                PsiElement iterable = source;
                /* resolve possible values for local variables */
                if (iterable instanceof Variable) {
                    final Function function = ExpressionSemanticUtil.getScope(loop);
                    if (function != null) {
                        final String variableName = ((Variable) iterable).getName();
                        final boolean isParameter = Stream.of(function.getParameters()).anyMatch(p -> variableName.equals(p.getName()));
                        if (!isParameter) {
                            final List<Variable> used   = ExpressionSemanticUtil.getUseListVariables(function);
                            final boolean isUseVariable = used != null && used.stream().anyMatch(u -> variableName.equals(u.getName()));
                            if (!isUseVariable) {
                                final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(iterable);
                                if (values.size() == 1) {
                                    iterable = values.iterator().next();
                                }
                                values.clear();
                            }
                        }
                    }
                }
                /* analyze the source */
                if (iterable instanceof ArrayCreationExpression && iterable.getChildren().length == 0) {
                    holder.registerProblem(source, String.format(patternEmptyArray, source.getText()));
                }
            }

            @NotNull
            private List<PsiElement> findUsedContainers(@NotNull For loop) {
                final List<PsiElement> result    = new ArrayList<>();
                final Set<String> loopVariables = this.getLoopVariables(loop);
                if (loopVariables.size() == 1) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                    if (body != null) {
                        PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class).forEach(access -> {
                            final PsiElement base = access.getValue();
                            if (base instanceof Variable || base instanceof FieldReference) {
                                final ArrayIndex index = access.getIndex();
                                final PsiElement key   = index == null ? null : index.getValue();
                                if (key instanceof Variable && loopVariables.contains(((Variable) key).getName())) {
                                    final boolean isKnown = result.stream().anyMatch(e -> OpenapiEquivalenceUtil.areEqual(e, access));
                                    if (!isKnown) {
                                        result.add(base);
                                    }
                                }
                            }
                        });
                    }
                }
                loopVariables.clear();
                return result;
            }

            private void inspectParentConditions(@NotNull PsiElement loop, @Nullable PsiElement source) {
                if (source instanceof Variable || source instanceof FieldReference) {
                    PsiElement parent = loop.getParent();
                    while (parent != null && !(parent instanceof Function) && !(parent instanceof PsiFile)) {
                        /* extract condition */
                        PsiElement condition = null;
                        if (parent instanceof If) {
                            condition = ((If) parent).getCondition();
                        } else if (parent instanceof ElseIf) {
                            condition = ((ElseIf) parent).getCondition();
                            parent    = parent.getParent(); /* skip if processing */
                        } else if (parent instanceof Else) {
                            parent    = parent.getParent(); /* skip if processing */
                        }
                        /* process condition and continue */
                        if (condition != null) {
                            final PsiElement anomaly = this.findFirstConditionAnomaly(source, condition);
                            if (anomaly != null && !this.isOverridden(source, condition.getParent())) {
                                holder.registerProblem(
                                        loop.getFirstChild(),
                                        String.format(patternConditionAnomaly, anomaly.getText())
                                );
                                break;
                            }
                        }
                        parent = parent.getParent();
                    }
                }
            }

            private boolean isOverridden(@NotNull PsiElement source, @NotNull PsiElement branch) {
                boolean result = false;
                for (final PsiElement child: PsiTreeUtil.findChildrenOfType(branch, source.getClass())) {
                    if (child == source) {
                        break;
                    }
                    final PsiElement parent = child.getParent();
                    if (OpenapiTypesUtil.isAssignment(parent) && OpenapiEquivalenceUtil.areEqual(source, child)) {
                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                        if (result = assignment.getValue() != child) {
                            break;
                        }
                    }
                }
                return result;
            }

            private PsiElement findFirstConditionAnomaly(@NotNull PsiElement source, @NotNull PsiElement condition) {
                for (final PsiElement expression : OpenapiPsiSearchUtil.findEqual(condition, source)) {
                    final PsiElement parent        = expression.getParent();
                    final PsiElement directContext = parent instanceof ParameterList ? parent.getParent() : parent;
                    final PsiElement outerContext  = directContext.getParent();

                    /* case: empty-statement */
                    if (directContext instanceof PhpEmpty) {
                        if (outerContext instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) outerContext;
                            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                return null;
                            }
                        } else if (outerContext instanceof BinaryExpression) {
                            final IElementType operation = ((BinaryExpression) outerContext).getOperationType();
                            if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                                return null;
                            }
                        }
                        return directContext;
                    }
                    /* case: empty checks */
                    else if (directContext instanceof UnaryExpression) {
                        final UnaryExpression unary = (UnaryExpression) directContext;
                        if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                            return directContext;
                        }
                    }
                    else if (directContext instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) directContext;
                        final IElementType operator   = binary.getOperationType();
                        if (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opIDENTICAL) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, expression);
                            if (second instanceof ArrayCreationExpression) {
                                final ArrayCreationExpression array = (ArrayCreationExpression) second;
                                if (array.getChildren().length == 0) {
                                    return binary;
                                }
                            }
                        }
                    }
                    /* case: count function/method */
                    else if (directContext instanceof FunctionReference) {
                        final FunctionReference call = (FunctionReference) directContext;
                        final String functionName    = call.getName();
                        if (functionName != null) {
                            if (outerContext instanceof BinaryExpression) {
                                if (functionName.equals("count")) {
                                    final BinaryExpression binary = (BinaryExpression) outerContext;
                                    if (call == binary.getLeftOperand()) {
                                        final PsiElement threshold = binary.getRightOperand();
                                        if (threshold != null && OpenapiTypesUtil.isNumber(threshold)) {
                                            final String number   = threshold.getText();
                                            final IElementType op = binary.getOperationType();
                                            if (op == PhpTokenTypes.opLESS && number.equals("2")) {
                                                return outerContext;
                                            }
                                            if (operationsAnomaly.contains(op) && (number.equals("0") || number.equals("1"))) {
                                                return outerContext;
                                            }
                                        }
                                    }
                                }
                            } else if (outerContext instanceof UnaryExpression) {
                                if (functionName.equals("count") || functionName.equals("is_array") || functionName.equals("is_iterable")) {
                                    final UnaryExpression unary = (UnaryExpression) outerContext;
                                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                        return outerContext;
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }

            private void inspectConditions(@NotNull For forStatement) {
                if (forStatement.getConditionalExpressions().length > 1) {
                    holder.registerProblem(forStatement.getFirstChild(), messageMultipleConditions);
                }
            }

            private void inspectBoundariesCorrectness(@NotNull For forStatement) {
                /* currently supported simple for-loops: one condition and one repeated operation */
                final PsiElement[] repeated   = forStatement.getRepeatedExpressions();
                final PsiElement[] conditions = forStatement.getConditionalExpressions();
                if (repeated.length != 1 || conditions.length != 1 || !(conditions[0] instanceof BinaryExpression)) {
                    return;
                }

                /* extract index and repeated operator */
                final PsiElement index;
                final IElementType repeatedOperator;
                if (repeated[0] instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) repeated[0];
                    final PsiElement operation  = unary.getOperation();
                    repeatedOperator            = operation == null ? null : operation.getNode().getElementType();
                    index                       = unary.getValue();
                } else if (repeated[0] instanceof SelfAssignmentExpression) {
                    final SelfAssignmentExpression selfAssign = (SelfAssignmentExpression) repeated[0];
                    repeatedOperator                          = selfAssign.getOperationType();
                    index                                     = selfAssign.getVariable();
                } else if (repeated[0] instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) repeated[0];
                    final PsiElement value                = assignment.getValue();
                    repeatedOperator                      = value instanceof BinaryExpression ? ((BinaryExpression) value).getOperationType() : null;
                    index                                 = assignment.getVariable();
                } else {
                    repeatedOperator = null;
                    index            = null;
                }
                if (repeatedOperator == null || index == null) {
                    return;
                }

                /* analyze condition and extract expected operations */
                final List<IElementType> expectedRepeatedOperator = new ArrayList<>();
                final BinaryExpression condition                  = (BinaryExpression) conditions[0];
                IElementType checkOperator                        = condition.getOperationType();

                /* false-positives: joda conditions applied, invert the operator */
                if (operationsInversion.containsKey(checkOperator)) {
                    final PsiElement right = condition.getRightOperand();
                    if (OpenapiTypesUtil.isNumber(condition.getLeftOperand())) {
                        checkOperator = operationsInversion.get(checkOperator);
                    } else if (right != null && OpenapiEquivalenceUtil.areEqual(index, right)) {
                        checkOperator = operationsInversion.get(checkOperator);
                    }
                }

                if (checkOperator == PhpTokenTypes.opGREATER || checkOperator == PhpTokenTypes.opGREATER_OR_EQUAL) {
                    expectedRepeatedOperator.add(PhpTokenTypes.opDECREMENT);
                    expectedRepeatedOperator.add(PhpTokenTypes.opMINUS);
                    expectedRepeatedOperator.add(PhpTokenTypes.opMINUS_ASGN);
                    expectedRepeatedOperator.add(PhpTokenTypes.opDIV_ASGN);
                }
                else if (checkOperator == PhpTokenTypes.opLESS || checkOperator == PhpTokenTypes.opLESS_OR_EQUAL) {
                    expectedRepeatedOperator.add(PhpTokenTypes.opINCREMENT);
                    expectedRepeatedOperator.add(PhpTokenTypes.opPLUS);
                    expectedRepeatedOperator.add(PhpTokenTypes.opPLUS_ASGN);
                    expectedRepeatedOperator.add(PhpTokenTypes.opMUL_ASGN);
                }

                if (!expectedRepeatedOperator.isEmpty()) {
                    if (!expectedRepeatedOperator.contains(repeatedOperator)) {
                        holder.registerProblem(forStatement.getFirstChild(), messageLoopBoundaries);
                    }
                    expectedRepeatedOperator.clear();
                }
            }

            private void inspectVariables(@NotNull PhpPsiElement loop) {
                final Set<String> loopVariables = this.getLoopVariables(loop);

                final Function function = ExpressionSemanticUtil.getScope(loop);
                if (null != function) {
                    final HashSet<String> parameters = new HashSet<>();
                    for (final Parameter param : function.getParameters()) {
                        parameters.add(param.getName());
                    }
                    loopVariables.forEach(variable -> {
                        if (parameters.contains(variable)) {
                            holder.registerProblem(
                                    loop.getFirstChild(),
                                    String.format(patternOverridesParameter, variable, function instanceof Method ? "method" : "function")
                            );
                        }
                    });
                    parameters.clear();
                }

                /* scan parents until reached file/callable */
                PsiElement parent = loop.getParent();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    /* inspect parent loops for conflicted variables */
                    if (parent instanceof For || parent instanceof ForeachStatement) {
                        final Set<String> parentVariables = this.getLoopVariables((PhpPsiElement) parent);
                        loopVariables.forEach(variable -> {
                            if (parentVariables.contains(variable)) {
                                holder.registerProblem(loop.getFirstChild(), String.format(patternOverridesLoopVars, variable));
                            }
                        });
                        parentVariables.clear();
                    }

                    parent = parent.getParent();
                }
                loopVariables.clear();
            }

            @NotNull
            private Set<String> getLoopVariables(@NotNull PhpPsiElement loop) {
                final Set<String> variables = new HashSet<>();
                if (loop instanceof For) {
                    /* get variables from assignments */
                    Stream.of(((For) loop).getInitialExpressions()).forEach(init -> {
                        if (init instanceof AssignmentExpression) {
                            final PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                            if (variable instanceof Variable) {
                                final String variableName = variable.getName();
                                if (variableName != null) {
                                    variables.add(variableName);
                                }
                            }
                        }
                    });
                } else if (loop instanceof ForeachStatement) {
                    ((ForeachStatement) loop).getVariables().forEach(variable -> variables.add(variable.getName()));
                }

                return variables;
            }
        };
    }
}