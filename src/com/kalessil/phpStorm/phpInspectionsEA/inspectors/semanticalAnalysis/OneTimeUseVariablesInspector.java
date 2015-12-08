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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;


public class OneTimeUseVariablesInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Variable $%v% is redundant";

    @NotNull
    public String getShortName() {
        return "OneTimeUseVariablesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void checkOneTimeUse(StatementWithArgument throwOrReturn, Variable argument) {
                String variableName = argument.getName();
                /* verify preceding expression (assignment needed) */
                if (
                    !StringUtil.isEmpty(variableName) && null != throwOrReturn.getPrevPsiSibling() &&
                    throwOrReturn.getPrevPsiSibling().getFirstChild() instanceof AssignmentExpression
                ) {
                    /* ensure variables are the same */
                    AssignmentExpression assign  = (AssignmentExpression) throwOrReturn.getPrevPsiSibling().getFirstChild();
                    /* skip self assignments */
                    if (assign instanceof SelfAssignmentExpression) {
                        return;
                    }

                    PhpPsiElement assignVariable = assign.getVariable();
                    if (assignVariable instanceof Variable) {
                        String assignVariableName = assignVariable.getName();
                        if (StringUtil.isEmpty(assignVariableName) || !assignVariableName.equals(variableName)) {
                            return;
                        }

                        final boolean isConstructDueToLongAssignment = assign.getText().length() > 80;
                        if (isConstructDueToLongAssignment) {
                            return;
                        }

                        /* heavy part, find usage inside function/method  to analyze multiple writes */
                        PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(assign);
                        if (null != parentScope) {
                            PhpEntryPointInstruction objEntryPoint = parentScope.getControlFlow().getEntryPoint();
                            PhpAccessVariableInstruction[] usages  = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, variableName, false);

                            int countWrites = 0;
                            for (PhpAccessVariableInstruction oneCase: usages) {
                                countWrites += oneCase.getAccess().isWrite() ? 1 : 0;
                                if (countWrites > 1) {
                                    return;
                                }
                            }
                        }

                        String message = strProblemDescription.replace("%v%", variableName);
                        holder.registerProblem(assignVariable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }

            public void visitPhpReturn(PhpReturn returnStatement) {
                PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(returnStatement.getArgument());
                if (argument instanceof Variable) {
                    checkOneTimeUse(returnStatement, (Variable) argument);
                }
            }

            public void visitPhpThrow(PhpThrow throwStatement) {
                PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(throwStatement.getArgument());
                if (argument instanceof Variable) {
                    checkOneTimeUse(throwStatement, (Variable) argument);
                }
            }
        };
    }
}

