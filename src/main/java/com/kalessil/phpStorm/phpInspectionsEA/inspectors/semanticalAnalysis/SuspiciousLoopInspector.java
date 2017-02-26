package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SuspiciousLoopInspector extends BasePhpInspection {
    private static final String messageMultipleConditions = "Please use && or || for multiple conditions. Currently no checks are performed after first positive result.";
    private static final String patternOverridesLoopVars  = "Variable '$%v%' is introduced in a outer loop and overridden here.";
    private static final String patternOverridesParameter = "Variable '$%v%' is introduced as a %t% parameter and overridden here.";
    private static final String messageLoopBoundaries     = "Conditions and repeated operations are not complimentary, please check what's going on here.";

    private static final Map<IElementType, IElementType> operationsInversion = new HashMap<>();
    static {
        operationsInversion.put(PhpTokenTypes.opGREATER,          PhpTokenTypes.opLESS_OR_EQUAL);
        operationsInversion.put(PhpTokenTypes.opGREATER_OR_EQUAL, PhpTokenTypes.opLESS);
        operationsInversion.put(PhpTokenTypes.opLESS,             PhpTokenTypes.opGREATER_OR_EQUAL);
        operationsInversion.put(PhpTokenTypes.opLESS_OR_EQUAL,    PhpTokenTypes.opGREATER);
    }

    @NotNull
    public String getShortName() {
        return "SuspiciousLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                this.inspectVariables(foreach);
            }
            public void visitPhpFor(For forStatement) {
                this.inspectConditions(forStatement);
                this.inspectVariables(forStatement);
                this.inspectBoundariesCorrectness(forStatement);
            }

            private void inspectConditions(For forStatement) {
                if (forStatement.getConditionalExpressions().length > 1) {
                    holder.registerProblem(forStatement.getFirstChild(), messageMultipleConditions, ProblemHighlightType.GENERIC_ERROR);
                }
            }

            private void inspectBoundariesCorrectness(For forStatement) {
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
                    if (null != left) {
                        final IElementType leftType = left.getNode().getElementType();
                        if (PhpElementTypes.NUMBER == leftType && operationsInversion.containsKey(checkOperator)) {
                            checkOperator = operationsInversion.get(checkOperator);
                        }
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

                holder.registerProblem(forStatement.getFirstChild(), messageLoopBoundaries, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }

            private void inspectVariables(PhpPsiElement loop) {
                final HashSet<String> loopVariables = getLoopVariables(loop);

                final Function function = ExpressionSemanticUtil.getScope(loop);
                if (null != function) {
                    final HashSet<String> parameters = new HashSet<>();
                    for (Parameter param : function.getParameters()) {
                        parameters.add(param.getName());
                    }

                    for (String variable : loopVariables) {
                        if (parameters.contains(variable)) {
                            final String message = patternOverridesParameter
                                    .replace("%v%", variable)
                                    .replace("%t%", function instanceof Method ? "method" : "function");
                            holder.registerProblem(loop.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                    parameters.clear();
                }

                /* scan parents until reached file/callable */
                PsiElement parent = loop.getParent();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    /* inspect parent loops for conflicted variables */
                    if (parent instanceof For || parent instanceof ForeachStatement) {
                        final HashSet<String> parentVariables = getLoopVariables((PhpPsiElement) parent);
                        for (String variable : loopVariables) {
                            if (parentVariables.contains(variable)) {
                                final String message = patternOverridesLoopVars.replace("%v%", variable);
                                holder.registerProblem(loop.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                        parentVariables.clear();
                    }

                    parent = parent.getParent();
                }
                loopVariables.clear();
            }

            private HashSet<String> getLoopVariables(PhpPsiElement loop) {
                final HashSet<String> loopVariables = new HashSet<>();

                if (loop instanceof For) {
                    /* find assignments in init expressions */
                    for (PhpPsiElement init : ((For) loop).getInitialExpressions()) {
                        if (init instanceof AssignmentExpression) {
                            /* variable used in assignment */
                            final PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                            if (variable instanceof Variable && null != variable.getName()) {
                                loopVariables.add(variable.getName());
                            }
                        }
                    }
                }

                if (loop instanceof ForeachStatement) {
                    /* just extract variables which created by foreach */
                    for (Variable variable : ((ForeachStatement) loop).getVariables()) {
                        loopVariables.add(variable.getName());
                    }
                }

                return loopVariables;
            }
        };
    }
}