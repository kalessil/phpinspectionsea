package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class SuspiciousLoopInspector extends BasePhpInspection {
    private static final String messageMultipleConditions = "Please use && or || for multiple conditions. Currently no checks are performed after first positive result.";
    private static final String messageLoopBoundaries     = "Conditions and repeated operations are not complimentary, please check what's going on here.";
    private static final String patternOverridesLoopVars  = "Variable '$%v%' is introduced in a outer loop and overridden here.";
    private static final String patternOverridesParameter = "Variable '$%v%' is introduced as a %t% parameter and overridden here.";
    private static final String patternConditionAnomaly   = "A parent condition '%s' looks suspicious.";


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
    public String getShortName() {
        return "SuspiciousLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement statement) {
                this.inspectVariables(statement);
                this.inspectParentConditions(statement);
            }

            @Override
            public void visitPhpFor(@NotNull For statement) {
                this.inspectConditions(statement);
                this.inspectVariables(statement);
                this.inspectBoundariesCorrectness(statement);
            }

            private void inspectParentConditions(@NotNull ForeachStatement statement) {
                final PsiElement source = statement.getArray();
                if (source instanceof Variable || source instanceof FieldReference) {
                    PsiElement parent = statement.getParent();
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
                            if (anomaly != null) {
                                final String message = String.format(patternConditionAnomaly, anomaly.getText());
                                holder.registerProblem(statement.getFirstChild(), message);

                                break;
                            }
                        }
                        parent = parent.getParent();
                    }
                }
            }

            private PsiElement findFirstConditionAnomaly(@NotNull PsiElement source, @NotNull PsiElement condition) {
                for (final PsiElement expression : OpenapiPsiSearchUtil.findEqual(condition, source)) {
                    final PsiElement parent        = expression.getParent();
                    final PsiElement directContext = parent instanceof ParameterList ? parent.getParent() : parent;
                    final PsiElement outerContext  = directContext.getParent();

                    /* case: empty statement */
                    if (directContext instanceof PhpEmpty) {
                        if (outerContext instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) outerContext;
                            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                return null;
                            }
                        } else if (outerContext instanceof BinaryExpression) {
                            final IElementType operation = ((BinaryExpression) outerContext).getOperationType();
                            /* skip analyzing comparisons - I don't want to invest my time into this right now*/
                            if (PhpTokenTypes.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                                return null;
                            }
                        }
                        return directContext;
                    }

                    /* case: count function/method */
                    if (outerContext instanceof BinaryExpression && directContext instanceof FunctionReference) {
                        final FunctionReference call = (FunctionReference) directContext;
                        final String functionName    = call.getName();
                        if (functionName != null && functionName.equals("count")) {
                            final BinaryExpression binary = (BinaryExpression) outerContext;
                            if (call == binary.getLeftOperand()) {
                                final PsiElement threshold = binary.getRightOperand();
                                if (OpenapiTypesUtil.isNumber(threshold)) {
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
                if (1 != repeated.length || 1 != conditions.length) {
                    return;
                }

                /* based on the condition, populate expected operations */
                final List<IElementType> expectedRepeatedOperator = new ArrayList<>();
                if (conditions[0] instanceof BinaryExpression) {
                    IElementType checkOperator = ((BinaryExpression) conditions[0]).getOperationType();

                    /* false-positives: joda conditions applied, invert the operator */
                    final PsiElement left = ((BinaryExpression) conditions[0]).getLeftOperand();
                    if (OpenapiTypesUtil.isNumber(left) && operationsInversion.containsKey(checkOperator)) {
                        checkOperator = operationsInversion.get(checkOperator);
                    }

                    if (checkOperator == PhpTokenTypes.opGREATER || checkOperator == PhpTokenTypes.opGREATER_OR_EQUAL) {
                        expectedRepeatedOperator.add(PhpTokenTypes.opDECREMENT);
                        expectedRepeatedOperator.add(PhpTokenTypes.opMINUS);
                        expectedRepeatedOperator.add(PhpTokenTypes.opMINUS_ASGN);
                    }
                    if (checkOperator == PhpTokenTypes.opLESS || checkOperator == PhpTokenTypes.opLESS_OR_EQUAL) {
                        expectedRepeatedOperator.add(PhpTokenTypes.opINCREMENT);
                        expectedRepeatedOperator.add(PhpTokenTypes.opPLUS);
                        expectedRepeatedOperator.add(PhpTokenTypes.opPLUS_ASGN);
                    }
                }
                if (expectedRepeatedOperator.isEmpty()) {
                    return;
                }

                final IElementType repeatedOperator;
                if (repeated[0] instanceof UnaryExpression) {
                    PsiElement operation = ((UnaryExpression) repeated[0]).getOperation();
                    repeatedOperator     = null == operation ? null : operation.getNode().getElementType();
                } else if (repeated[0] instanceof SelfAssignmentExpression) {
                    repeatedOperator = ((SelfAssignmentExpression) repeated[0]).getOperationType();
                } else if (repeated[0] instanceof AssignmentExpression) {
                    PsiElement value = ((AssignmentExpression) repeated[0]).getValue();
                    repeatedOperator = value instanceof BinaryExpression ? ((BinaryExpression) value).getOperationType() : null;
                } else {
                    repeatedOperator = null;
                }
                if (null == repeatedOperator || expectedRepeatedOperator.contains(repeatedOperator)) {
                    expectedRepeatedOperator.clear();
                    return;
                }

                holder.registerProblem(forStatement.getFirstChild(), messageLoopBoundaries);
            }

            private void inspectVariables(@NotNull PhpPsiElement loop) {
                final Set<String> loopVariables = this.getLoopVariables(loop);

                final Function function = ExpressionSemanticUtil.getScope(loop);
                if (null != function) {
                    final HashSet<String> parameters = new HashSet<>();
                    for (Parameter param : function.getParameters()) {
                        parameters.add(param.getName());
                    }

                    loopVariables.stream()
                            .filter(parameters::contains)
                            .forEach(variable -> {
                                final String message = patternOverridesParameter
                                    .replace("%v%", variable)
                                    .replace("%t%", function instanceof Method ? "method" : "function");
                                holder.registerProblem(loop.getFirstChild(), message);
                            });
                    parameters.clear();
                }

                /* scan parents until reached file/callable */
                PsiElement parent = loop.getParent();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    /* inspect parent loops for conflicted variables */
                    if (parent instanceof For || parent instanceof ForeachStatement) {
                        final Set<String> parentVariables = this.getLoopVariables((PhpPsiElement) parent);
                        loopVariables.stream()
                                .filter(parentVariables::contains)
                                .forEach(variable -> {
                                    final String message = patternOverridesLoopVars.replace("%v%", variable);
                                    holder.registerProblem(loop.getFirstChild(), message);
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
                    Stream.of(((For) loop).getInitialExpressions())
                            .filter(init  -> init instanceof AssignmentExpression)
                            .forEach(init -> {
                                final PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                                if (variable instanceof Variable) {
                                    final String variableName = variable.getName();
                                    if (variableName != null) {
                                        variables.add(variableName);
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