package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class CallableParameterUseCaseInTypeContextInspection extends BasePhpInspection {
    private static final String strProblemNoSense = "Has no sense, because it's always true (according to annotations)";
    private static final String strProblemCheckViolatesDefinition = "Has no sense, because this type in not defined in annotations";
    private static final String strProblemAssignmentViolatesDefinition = "This assignment type violates types set defined in annotations";

    @NotNull
    public String getDisplayName() {
        return "Semantics: callable parameter usages in type context";
    }

    @NotNull
    public String getShortName() {
        return "CallableParameterUseCaseInTypeContextInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                this.inspectUsages(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.inspectUsages(function.getParameters(), function);
            }

            /**
             * @param arrParameters
             * @param objScopeHolder
             */
            private void inspectUsages(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                for (Parameter objParameter : arrParameters) {
                    String strParameterName = objParameter.getName();
                    String strParameterType = objParameter.getType().toString();
                    if (
                        StringUtil.isEmpty(strParameterName) ||
                        StringUtil.isEmpty(strParameterType) ||
                        strParameterType.contains("mixed") ||   /** fair enough */
                        strParameterType.contains("#")          /** TODO: types lookup */
                    ) {
                        continue;
                    }

                    /** too lazy to do anything more elegant */
                    strParameterType = strParameterType.replace("callable", "array|string");

                    PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                    if (arrUsages.length == 0) {
                        continue;
                    }

                    PsiElement objExpression;
                    FunctionReference objFunctionCall;
                    /** TODO: dedicate to method */
                    for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                        objExpression = objInstruction.getAnchor().getParent();

                        /** inspect type checks are used */
                        if (
                            objExpression instanceof ParameterList &&
                            objExpression.getParent() instanceof FunctionReference
                        ) {
                            objFunctionCall = (FunctionReference) objExpression.getParent();
                            String strFunctionName = objFunctionCall.getName();
                            if (StringUtil.isEmpty(strFunctionName)) {
                                continue;
                            }

                            boolean isReversedCheck = false;
                            if (objFunctionCall.getParent() instanceof UnaryExpression) {
                                UnaryExpression objCallWrapper = (UnaryExpression) objFunctionCall.getParent();
                                isReversedCheck = (
                                    null != objCallWrapper.getOperation() &&
                                    objCallWrapper.getOperation().getText().equals("!")
                                );
                            }

                            boolean isCallHasNoSense;
                            boolean isCallViolatesDefinition;
                            boolean isTypeAnnounced;

                            if (strFunctionName.equals("is_array")) {
                                isTypeAnnounced = strParameterType.contains("array") || strParameterType.contains("[]");
                            } else if (strFunctionName.equals("is_string")) {
                                isTypeAnnounced = strParameterType.contains("string");
                            } else if (strFunctionName.equals("is_bool")) {
                                isTypeAnnounced = strParameterType.contains("bool");
                            } else if (strFunctionName.equals("is_int") || strFunctionName.equals("is_integer")) {
                                isTypeAnnounced = strParameterType.contains("int");
                            } else if (strFunctionName.equals("is_float")) {
                                isTypeAnnounced = strParameterType.contains("float");
                            } else if (strFunctionName.equals("is_resource")) {
                                isTypeAnnounced = strParameterType.contains("resource");
                            } else {
                                continue;
                            }

                            isCallHasNoSense = !isTypeAnnounced && isReversedCheck;
                            if (isCallHasNoSense) {
                                holder.registerProblem(objFunctionCall, strProblemNoSense, ProblemHighlightType.ERROR);
                                continue;
                            }

                            isCallViolatesDefinition = !isTypeAnnounced;
                            if (isCallViolatesDefinition) {
                                holder.registerProblem(objFunctionCall, strProblemCheckViolatesDefinition, ProblemHighlightType.ERROR);
                                continue;
                            }

                            continue;
                        }

                        /** check if assignments not violating defined interfaces */
                        /** TODO: dedicate to method */
                        if (objExpression instanceof AssignmentExpression) {
                            AssignmentExpression objAssignment = (AssignmentExpression) objExpression;

                            PhpPsiElement objVariable = objAssignment.getVariable();
                            PhpPsiElement objValue = objAssignment.getValue();
                            if (null == objVariable || null == objValue) {
                                continue;
                            }

                            if (
                                objVariable instanceof Variable &&
                                null != objVariable.getName() && objVariable.getName().equals(strParameterName)
                            ) {
                                LinkedList<String> objTypesResolved = new LinkedList<>();
                                TypeFromPsiResolvingUtil.resolveExpressionType(objValue, objIndex, objTypesResolved);
                                /** TODO: make unique */

                                boolean isCallViolatesDefinition;
                                for (String strType : objTypesResolved) {
                                    if (
                                        strType.equals(Types.strResolvingAbortedOnPsiLevel) ||
                                        strType.equals(Types.strMixed)
                                    ) {
                                        continue;
                                    }

                                    /** TODO: lookup if resolved is class and extended from a defined class */
                                    isCallViolatesDefinition = !strParameterType.contains(strType);
                                    if (isCallViolatesDefinition) {
                                        holder.registerProblem(objValue, strProblemAssignmentViolatesDefinition + ": " + strType, ProblemHighlightType.ERROR);
                                        break;
                                    }
                                }
                                objTypesResolved.clear();
                            }
                        }
                        /* TODO: can be analysed comparison operations, instanceof */
                    }
                }
            }
        };
    }
}
