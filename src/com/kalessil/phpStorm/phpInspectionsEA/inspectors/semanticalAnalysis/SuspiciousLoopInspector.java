package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class SuspiciousLoopInspector extends BasePhpInspection {
    private static final String strProblemMultipleConditions = "Please use && or || for multiple conditions. Currently no checks are performed after first positive result.";
    private static final String strProblemDescription = "Variable $%v% is introduced in a outer loop and overridden here";

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
                this.inspectVariables(forStatement);
                this.inspectConditions(forStatement);
            }

            private void inspectConditions(For forStatement) {
                if (forStatement.getConditionalExpressions().length > 1) {
                    holder.registerProblem(forStatement.getFirstChild(), strProblemMultipleConditions, ProblemHighlightType.GENERIC_ERROR);
                }
            }

            private void inspectVariables(PhpPsiElement objLoop) {
                HashSet<String> loopVariables = getLoopVariables(objLoop);

                /** scan parents until reached file/callable */
                PsiElement parent = objLoop.getParent();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    /** inspect parent loops for conflicted variables */
                    if (parent instanceof For || parent instanceof ForeachStatement) {
                        HashSet<String> parentVariables = getLoopVariables((PhpPsiElement) parent);
                        for (String variable : loopVariables) {
                            if (parentVariables.contains(variable)) {
                                String strWarning = strProblemDescription.replace("%v%", variable);
                                holder.registerProblem(objLoop.getFirstChild(), strWarning, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                        parentVariables.clear();
                    }

                    parent = parent.getParent();
                }

                loopVariables.clear();
            }

            private HashSet<String> getLoopVariables(PhpPsiElement objLoop) {
                HashSet<String> loopVariables = new HashSet<String>();

                if (objLoop instanceof For) {
                    /** find assignments in init expressions */
                    for (PhpPsiElement init : ((For) objLoop).getInitialExpressions()) {
                        if (init instanceof AssignmentExpression) {
                            /** variable used in assignment */
                            PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                            if (variable instanceof Variable && null != variable.getName()) {
                                loopVariables.add(variable.getName());
                            }
                        }
                    }
                }

                if (objLoop instanceof ForeachStatement) {
                    /** just extract variables which created by foreach */
                    for (Variable variable : ((ForeachStatement) objLoop).getVariables()) {
                        if (null != variable.getName()) {
                            loopVariables.add(variable.getName());
                        }
                    }
                }

                return loopVariables;
            }
        };
    }
}