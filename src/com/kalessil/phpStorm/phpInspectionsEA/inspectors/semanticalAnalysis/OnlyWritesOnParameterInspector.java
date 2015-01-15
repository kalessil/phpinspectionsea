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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class OnlyWritesOnParameterInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Parameter is overridden, but never used or appears " +
            "outside of the scope";

    @NotNull
    public String getDisplayName() {
        return "Unused: callable parameter is not used";
    }

    @NotNull
    public String getShortName() {
        return "OnlyWritesOnParameterInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /** re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.getUnusedParameters(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.getUnusedParameters(function.getParameters(), function);
            }

            private void getUnusedParameters(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                for (Parameter objParameter : arrParameters) {
                    if (objParameter.isPassByRef()) {
                        continue;
                    }

                    String parameterName = objParameter.getName();
                    if (StringUtil.isEmpty(parameterName)) {
                        continue;
                    }

                    PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();
                    PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, parameterName, false);
                    if (arrUsages.length == 0) {
                        continue;
                    }

                    LinkedList<PsiElement> objTargetExpressions = new LinkedList<>();

                    int intCountReadAccesses = 0;
                    int intCountWriteAccesses = 0;
                    PhpAccessInstruction.Access objAccess;
                    for (PhpAccessVariableInstruction objInstruction : arrUsages) {

                        if (objInstruction.getAnchor().getParent() instanceof ArrayAccessExpression) {
                            /** find out which expression is holder */
                            PsiElement objLastSemanticExpression = objInstruction.getAnchor();
                            PsiElement objTopSemanticExpression = objLastSemanticExpression.getParent();
                            /** TODO: iterator for array access expression */
                            while (objTopSemanticExpression instanceof ArrayAccessExpression) {
                                objLastSemanticExpression = objTopSemanticExpression;
                                objTopSemanticExpression = objTopSemanticExpression.getParent();
                            }

                            /** estimate operation type */
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


                        /** ok variable usage works well with openapi */
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
            }
        };
    }
}