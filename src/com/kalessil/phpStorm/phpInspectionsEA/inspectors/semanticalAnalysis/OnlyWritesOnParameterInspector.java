package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class OnlyWritesOnParameterInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Parameter/variable is overridden, but never used or appears " +
            "outside of the scope";

    @NotNull
    public String getShortName() {
        return "OnlyWritesOnParameterInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.checkParameters(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.checkParameters(function.getParameters(), function);
            }

            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PsiElement objVariable = assignmentExpression.getVariable();
                /* check assignments containing variable as container */
                if (objVariable instanceof Variable) {
                    String variableName = ((Variable) objVariable).getName();
                    if (
                        StringUtil.isEmpty(variableName) ||
                        "|_GET|_POST|_SESSION|_REQUEST|_FILES|_COOKIE|_ENV|_SERVER|".contains("|" + variableName + "|")
                    ) {
                        return;
                    }

                    /* expression is located in function/method */
                    PsiElement parentScope = ExpressionSemanticUtil.getScope(assignmentExpression);
                    if (null != parentScope) {
                        /* ensure it's not parameter, as it checked anyway */
                        for (Parameter objParameter : ((Function) parentScope).getParameters()) {
                            String parameterName = objParameter.getName();
                            if (StringUtil.isEmpty(parameterName)) {
                                continue;
                            }

                            /* skip assignment check - it writes to parameter */
                            if (parameterName.equals(variableName)) {
                                return;
                            }
                        }

                        /* ensure it's not use list parameter of closure */
                        LinkedList<Variable> useList = ExpressionSemanticUtil.getUseListVariables((Function) parentScope);
                        if (null != useList) {
                            /* use-list is found */
                            for (Variable objUseVariable : useList) {
                                String useVariableName = objUseVariable.getName();
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
                        checkOneVariable(variableName, (PhpScopeHolder) parentScope);
                    }
                }
            }

            private void checkParameters(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                for (Parameter objParameter : arrParameters) {
                    if (objParameter.isPassByRef()) {
                        continue;
                    }

                    String parameterName = objParameter.getName();
                    if (StringUtil.isEmpty(parameterName)) {
                        continue;
                    }

                    checkOneVariable(parameterName, objScopeHolder);
               }
            }

            private void checkOneVariable(String parameterName, PhpScopeHolder objScopeHolder) {
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();
                PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, parameterName, false);
                if (arrUsages.length == 0) {
                    return;
                }

                LinkedList<PsiElement> objTargetExpressions = new LinkedList<PsiElement>();

                int intCountReadAccesses  = 0;
                int intCountWriteAccesses = 0;
                PhpAccessInstruction.Access objAccess;
                for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                    PsiElement objParent = objInstruction.getAnchor().getParent();

                    if (objParent instanceof ArrayAccessExpression) {
                        /* find out which expression is holder */
                        PsiElement objLastSemanticExpression = objInstruction.getAnchor();
                        PsiElement objTopSemanticExpression = objLastSemanticExpression.getParent();
                        /* TODO: iterator for array access expression */
                        while (objTopSemanticExpression instanceof ArrayAccessExpression) {
                            objLastSemanticExpression = objTopSemanticExpression;
                            objTopSemanticExpression = objTopSemanticExpression.getParent();
                        }

                        /* estimate operation type */
                        if (
                            objTopSemanticExpression instanceof AssignmentExpression &&
                            ((AssignmentExpression) objTopSemanticExpression).getVariable() == objLastSemanticExpression
                        ) {
                            objTargetExpressions.add(objLastSemanticExpression);

                            intCountWriteAccesses++;
                            continue;
                        }

                        if (objTopSemanticExpression instanceof UnaryExpression) {
                            PsiElement objOperation = ((UnaryExpression) objTopSemanticExpression).getOperation();
                            if (null != objOperation && ("++,--").contains(objOperation.getText())) {
                                objTargetExpressions.add(objLastSemanticExpression);

                                intCountWriteAccesses++;
                                continue;
                            }
                        }

                        intCountReadAccesses++;
                        continue;
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
                        holder.registerProblem(objTargetExpression, strProblemDescription, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
                objTargetExpressions.clear();
            }
        };
    }
}