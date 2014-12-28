package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class CallableParameterUseCaseInTypeContextInspection extends BasePhpInspection {
    private static final String strProblemNoSense = "Has no sense, because it's always true (according to annotations)";
    private static final String strProblemViolatesDefinition = "Has no sense, because this type in not defined in annotations";

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
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
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
                for (Parameter objParameter : arrParameters) {
                    String strParameterName = objParameter.getName();
                    String strParameterType = objParameter.getType().toString();
                    if (StringUtil.isEmpty(strParameterName) || StringUtil.isEmpty(strParameterType)) {
                        continue;
                    }

                    PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();
                    PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                    if (arrUsages.length == 0) {
                        continue;
                    }

                    PsiElement objExpression;
                    FunctionReference objFunctionCall;
                    for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                        objExpression = objInstruction.getAnchor().getParent();
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

                            /*holder.registerProblem(
                                    objExpression,
                                    "Used " + strParameterName + " announced as " + strParameterType,
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                            );*/

                            boolean isCallHasNoSense;
                            boolean isCallViolatesDefinition;
                            boolean isTypeAnnounced;

                            if (strFunctionName.equals("is_array")) {
                                isTypeAnnounced = strParameterType.contains("array");
                            } else if (strFunctionName.equals("is_string")) {
                                isTypeAnnounced = strParameterType.contains("string");
                            } else if (strFunctionName.equals("is_bool")) {
                                isTypeAnnounced = strParameterType.contains("bool");
                            } else if (strFunctionName.equals("is_int") || strFunctionName.equals("is_integer")) {
                                isTypeAnnounced = strParameterType.contains("int");
                            } else if (strFunctionName.equals("is_float")) {
                                isTypeAnnounced = strParameterType.contains("float");
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
                                holder.registerProblem(objFunctionCall, strProblemViolatesDefinition, ProblemHighlightType.ERROR);
                                continue;
                            }
                        }

                        /* TODO: can be analysed comparison operations, assignments */
                    }
                }
            }
        };
    }
}
