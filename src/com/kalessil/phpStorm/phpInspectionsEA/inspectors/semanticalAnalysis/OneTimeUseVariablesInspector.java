package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


public class OneTimeUseVariablesInspector extends BasePhpInspection {
    private static final String messagePattern = "Variable $%v% is redundant";

    @NotNull
    public String getShortName() {
        return "OneTimeUseVariablesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void checkOneTimeUse(@NotNull StatementWithArgument returnOrThrow, @NotNull Variable argument) {
                final String variableName = argument.getName();
                /* verify preceding expression (assignment needed) */
                if (
                    !StringUtil.isEmpty(variableName) && null != returnOrThrow.getPrevPsiSibling() &&
                    returnOrThrow.getPrevPsiSibling().getFirstChild() instanceof AssignmentExpression
                ) {
                    /* ensure variables are the same */
                    final AssignmentExpression assign = (AssignmentExpression) returnOrThrow.getPrevPsiSibling().getFirstChild();
                    /* skip self assignments */
                    if (assign instanceof SelfAssignmentExpression || assign instanceof MultiassignmentExpression) {
                        return;
                    }

                    final PhpPsiElement assignVariable = assign.getVariable();
                    final PsiElement assignValue       = ExpressionSemanticUtil.getExpressionTroughParenthesis(assign.getValue());
                    if (assignVariable instanceof Variable && null != assignValue) {
                        final String assignVariableName = assignVariable.getName();
                        if (StringUtil.isEmpty(assignVariableName) || !assignVariableName.equals(variableName)) {
                            return;
                        }

                        /* check if variable as a function/use(...) parameter by reference */
                        final Function function = ExpressionSemanticUtil.getScope(returnOrThrow);
                        if (null != function) {
                            for (Parameter param: function.getParameters()) {
                                if (param.isPassByRef() && param.getName().equals(variableName)) {
                                    return;
                                }
                            }

                            LinkedList<Variable> useList = ExpressionSemanticUtil.getUseListVariables(function);
                            if (null != useList) {
                                for (Variable param: useList) {
                                    if (param.getName().equals(variableName) && param.getText().contains("&")) {
                                        return;
                                    }
                                }
                                useList.clear();
                            }
                        }

                        /* too long return/throw statements can be decoupled as a variable */
                        final boolean isConstructDueToLongAssignment = assign.getText().length() > 80;
                        if (isConstructDueToLongAssignment) {
                            return;
                        }

                        /* heavy part, find usage inside function/method to analyze multiple writes */
                        final PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(assign);
                        if (null != parentScope) {
                            final PhpEntryPointInstruction objEntryPoint = parentScope.getControlFlow().getEntryPoint();
                            final PhpAccessVariableInstruction[] usages  = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, variableName, false);

                            int countWrites = 0;
                            for (PhpAccessVariableInstruction oneCase: usages) {
                                countWrites += oneCase.getAccess().isWrite() ? 1 : 0;
                                if (countWrites > 1) {
                                    return;
                                }
                            }
                        }

                        final String message = messagePattern.replace("%v%", variableName);
                        final TheLocalFix fixer = new TheLocalFix(assign.getParent(), returnOrThrow, assignValue);
                        holder.registerProblem(assignVariable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixer);
                    }
                }
            }

            public void visitPhpReturn(PhpReturn returnStatement) {
                /* if function returning reference, do not inspect returns */
                final Function callable = ExpressionSemanticUtil.getScope(returnStatement);
                if (null != callable && null != callable.getNameIdentifier()) {
                    /* is defined like returning reference */
                    PsiElement referenceCandidate = callable.getNameIdentifier().getPrevSibling();
                    if (referenceCandidate instanceof PsiWhiteSpace) {
                        referenceCandidate = referenceCandidate.getPrevSibling();
                    }
                    if (null != referenceCandidate && PhpTokenTypes.opBIT_AND == referenceCandidate.getNode().getElementType()) {
                        return;
                    }
                }

                /* regular function, check one-time use variables */
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(returnStatement.getArgument());
                if (argument instanceof Variable) {
                    checkOneTimeUse(returnStatement, (Variable) argument);
                }
            }

            public void visitPhpThrow(PhpThrow throwStatement) {
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(throwStatement.getArgument());
                if (argument instanceof Variable) {
                    checkOneTimeUse(throwStatement, (Variable) argument);
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement assignment;
        private PsiElement value;
        private StatementWithArgument returnOrThrow;

        TheLocalFix(@NotNull PsiElement assignment, @NotNull StatementWithArgument returnOrThrow, @NotNull PsiElement value) {
            super();
            this.assignment    = assignment;
            this.returnOrThrow = returnOrThrow;
            this.value         = value;
        }

        @NotNull
        @Override
        public String getName() {
            return "Inline value";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (null != this.returnOrThrow.getArgument()) {
                /* delete preceding PhpDoc */
                final PhpPsiElement previous = ((StatementImpl) this.assignment).getPrevPsiSibling();
                if (previous instanceof PhpDocCommentImpl) {
                    previous.delete();
                }

                /* delete space after the method */
                PsiElement nextExpression = this.assignment.getNextSibling();
                if (nextExpression instanceof PsiWhiteSpace) {
                    nextExpression.delete();
                }

                /* delete assignment itself */
                this.returnOrThrow.getArgument().replace(this.value);
                this.assignment.delete();
            }
        }
    }
}

