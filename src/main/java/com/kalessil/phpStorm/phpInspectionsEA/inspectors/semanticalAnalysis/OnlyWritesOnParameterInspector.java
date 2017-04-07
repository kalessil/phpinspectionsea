package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OnlyWritesOnParameterInspector extends BasePhpInspection {
    private static final String messageOnlyWrites = "Parameter/variable is overridden, but is never used or appears outside of the scope.";
    private static final String messageUnused     = "The variable seems to be not used.";

    @NotNull
    public String getShortName() {
        return "OnlyWritesOnParameterInspection";
    }

    static private PhpAccessVariableInstruction[] getVariablesAccessInstructions(String parameterName, PhpScopeHolder objScopeHolder) {
        PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();
        return PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, parameterName, false);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.checkParameters(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.checkParameters(function.getParameters(), function);

                final List<Variable> variables = ExpressionSemanticUtil.getUseListVariables(function);
                if (null != variables) {
                    this.checkUseVariables(variables, function);
                }
            }

            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                final PsiElement objVariable = assignmentExpression.getVariable();
                /* check assignments containing variable as container */
                if (objVariable instanceof Variable) {
                    final String variableName = ((Variable) objVariable).getName();
                    if (StringUtil.isEmpty(variableName) || ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                        return;
                    }

                    /* expression is located in function/method */
                    final PsiElement parentScope = ExpressionSemanticUtil.getScope(assignmentExpression);
                    if (null != parentScope) {
                        /* ensure it's not parameter, as it checked anyway */
                        for (Parameter objParameter : ((Function) parentScope).getParameters()) {
                            final String parameterName = objParameter.getName();
                            if (StringUtil.isEmpty(parameterName)) {
                                continue;
                            }

                            /* skip assignment check - it writes to parameter */
                            if (parameterName.equals(variableName)) {
                                return;
                            }
                        }

                        /* ensure it's not use list parameter of closure */
                        final List<Variable> useList = ExpressionSemanticUtil.getUseListVariables((Function) parentScope);
                        if (null != useList) {
                            /* use-list is found */
                            for (Variable useVariable : useList) {
                                final String useVariableName = useVariable.getName();
                                if (StringUtil.isEmpty(useVariableName)) {
                                    continue;
                                }

                                /* skip assignment check - it writes to used variable */
                                if (useVariableName.equals(variableName)) {
                                    useList.clear();
                                    return;
                                }
                            }
                            useList.clear();
                        }

                        /* verify variable usage */
                        analyzeAndReturnUsagesCount(variableName, (PhpScopeHolder) parentScope);
                    }
                }
            }

            private void checkParameters(Parameter[] parameters, @NotNull PhpScopeHolder scopeHolder) {
                for (Parameter parameter : parameters) {
                    if (parameter.isPassByRef()) {
                        continue;
                    }

                    final String parameterName = parameter.getName();
                    if (StringUtil.isEmpty(parameterName)) {
                        continue;
                    }

                    analyzeAndReturnUsagesCount(parameterName, scopeHolder);
               }
            }

            private void checkUseVariables(@NotNull List<Variable> variables, @NotNull PhpScopeHolder scopeHolder) {
                for (Variable variable : variables) {
                    PsiElement previous = variable.getPrevSibling();
                    if (previous instanceof PsiWhiteSpace) {
                        previous = previous.getPrevSibling();
                    }

                    final String parameterName = variable.getName();
                    if (StringUtil.isEmpty(parameterName)) {
                        continue;
                    }

                    if (null != previous && PhpTokenTypes.opBIT_AND == previous.getNode().getElementType()) {
                        PhpAccessVariableInstruction[] arrUsages = getVariablesAccessInstructions(parameterName, scopeHolder);
                        if (0 == arrUsages.length) {
                            holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }

                        continue;
                    }

                    final int variableUsages = analyzeAndReturnUsagesCount(parameterName, scopeHolder);
                    if (0 == variableUsages) {
                        holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
            }

            private int analyzeAndReturnUsagesCount(String parameterName, PhpScopeHolder objScopeHolder) {
                PhpAccessVariableInstruction[] arrUsages = getVariablesAccessInstructions(parameterName, objScopeHolder);
                if (0 == arrUsages.length) {
                    return 0;
                }

                final List<PsiElement> objTargetExpressions = new ArrayList<>();

                boolean isReference       = false;
                int intCountReadAccesses  = 0;
                int intCountWriteAccesses = 0;
                PhpAccessInstruction.Access objAccess;
                for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                    PsiElement objParent = objInstruction.getAnchor().getParent();

                    if (objParent instanceof ArrayAccessExpression) {
                        /* find out which expression is holder */
                        PsiElement objLastSemanticExpression = objInstruction.getAnchor();
                        PsiElement objTopSemanticExpression  = objLastSemanticExpression.getParent();
                        /* TODO: iterator for array access expression */
                        while (objTopSemanticExpression instanceof ArrayAccessExpression) {
                            objLastSemanticExpression = objTopSemanticExpression;
                            objTopSemanticExpression  = objTopSemanticExpression.getParent();
                        }

                        /* estimate operation type */
                        if (
                            objTopSemanticExpression instanceof AssignmentExpression &&
                            ((AssignmentExpression) objTopSemanticExpression).getVariable() == objLastSemanticExpression
                        ) {
                            intCountWriteAccesses++;
                            if (isReference) {
                                /* when modifying the reference it's link READ and linked WRITE semantics */
                                intCountReadAccesses++;
                            } else {
                                /* when modifying non non-reference, register as write only access for reporting */
                                objTargetExpressions.add(objLastSemanticExpression);
                            }

                            continue;
                        }

                        if (objTopSemanticExpression instanceof UnaryExpression) {
                            final PsiElement objOperation = ((UnaryExpression) objTopSemanticExpression).getOperation();
                            if (null != objOperation && ("++,--").contains(objOperation.getText())) {
                                objTargetExpressions.add(objLastSemanticExpression);

                                intCountWriteAccesses++;
                                continue;
                            }
                        }

                        intCountReadAccesses++;
                        continue;
                    }

                    /* if variable assigned with reference, we need to preserve this information for correct checks */
                    if (objParent instanceof AssignmentExpression) {
                        /* ensure variable with the same name being written */
                        final AssignmentExpression referenceAssignmentCandidate = (AssignmentExpression) objParent;
                        if (referenceAssignmentCandidate.getVariable() instanceof Variable) {
                            final Variable sameVariableCandidate = (Variable) referenceAssignmentCandidate.getVariable();
                            final String candidateVariableName   = sameVariableCandidate.getName();
                            if (!StringUtil.isEmpty(candidateVariableName) && candidateVariableName.equals(parameterName)) {
                                /* now ensure operation is assignment of reference */
                                PsiElement operation = sameVariableCandidate.getNextSibling();
                                if (operation instanceof PsiWhiteSpace) {
                                    operation = operation.getNextSibling();
                                }

                                if (null != operation && operation.getText().replaceAll("\\s+", "").equals("=&")) {
                                    intCountWriteAccesses++;
                                    isReference = true;

                                    continue;
                                }
                            }
                        }
                    }

                    /* local variables access wrongly reported write in some cases, so rely on custom checks */
                    if (
                        objParent instanceof ParameterList ||
                        objParent instanceof PhpUseList ||
                        objParent instanceof PhpUnset ||
                        objParent instanceof PhpEmpty ||
                        objParent instanceof PhpIsset ||
                        objParent instanceof ForeachStatement
                    ) {
                        intCountReadAccesses++;
                        continue;
                    }


                    /* ok variable usage works well with openapi */
                    objAccess = objInstruction.getAccess();
                    if (objAccess.isWrite()) {
                        objTargetExpressions.add(objInstruction.getAnchor());
                        intCountWriteAccesses++;
                    }
                    if (objAccess.isRead()) {
                        intCountReadAccesses++;
                    }
                }


                if (intCountReadAccesses == 0 && intCountWriteAccesses > 0) {
                    for (PsiElement objTargetExpression : objTargetExpressions) {
                        holder.registerProblem(objTargetExpression, messageOnlyWrites, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
                objTargetExpressions.clear();

                return arrUsages.length;
            }
        };
    }
}
