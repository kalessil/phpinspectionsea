package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

public class DisconnectedForeachInstructionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This statement seems to be not connected with parent foreach";

    @NotNull
    public String getShortName() {
        return "DisconnectedForeachInstructionInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                Variable value             = foreach.getValue();
                GroupStatement foreachBody = ExpressionSemanticUtil.getGroupStatement(foreach);
                /* ensure foreach structure is ready for inspection */
                if (null != foreachBody && null != value && null != value.getName()) {
                    /* pre-collect introduced and internally used variables */
                    HashSet<String> allModifiedVariables = new HashSet<String>();
                    allModifiedVariables.add(value.getName());
                    Variable key = foreach.getKey();
                    if (null != key && null != key.getName()) {
                        allModifiedVariables.add(key.getName());
                    }

                    HashSet<String> allReadVariables = new HashSet<String>();
                    allReadVariables.add("this");

                    HashMap<PsiElement, HashSet<String>> instructionDependencies = new HashMap<PsiElement, HashSet<String>>();
                    /* iteration 1 - investigate what are dependencies and influence */
                    for (PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            HashSet<String> individualDependencies = new HashSet<String>();
                            individualDependencies.add("this");

                            instructionDependencies.put(oneInstruction, individualDependencies);
                            investigateInfluence((PhpPsiElement) oneInstruction, individualDependencies, allModifiedVariables);
                        }
                    }

                    /* iteration 2 - analyse dependencies */
                    for (PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement) {
                            boolean isDependOnModifiedVariables = false;

                            /* check if any dependency is overridden */
                            HashSet<String> individualDependencies = instructionDependencies.get(oneInstruction);
                            if (null != individualDependencies && individualDependencies.size() > 1) {
                                /* contains not only this */
                                for (String dependencyName : individualDependencies) {
                                    if (allModifiedVariables.contains(dependencyName)) {
                                        isDependOnModifiedVariables = true;
                                        break;
                                    }
                                }
                            }

                            /* verify and report if violation detected */
                            if (null != individualDependencies && !isDependOnModifiedVariables) {
                                boolean shallReport = !(oneInstruction instanceof If);
                                /**
                                 * TODO: assign/append string, clone
                                 */

                                if (shallReport) {
                                    holder.registerProblem(oneInstruction, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                                }
                            }

                            /* cleanup dependencies details */
                            if (null != individualDependencies) {
                                individualDependencies.clear();
                            }
                        }
                    }
                    /* empty dependencies details container */
                    instructionDependencies.clear();
                }
            }

            private void investigateInfluence(
                PhpPsiElement oneInstruction,
                HashSet<String> individualDependencies,
                HashSet<String> allModifiedVariables
            ) {
                for (PsiElement variable : PsiTreeUtil.findChildrenOfType(oneInstruction, Variable.class)) {
                    Variable castedVariable = (Variable) variable;
                    String variableName = castedVariable.getName();
                    if (null != variableName) {
                        if (variable.getParent() instanceof AssignmentExpression) {
                            AssignmentExpression assignment = (AssignmentExpression) variable.getParent();
                            if (assignment.getVariable() == variable) {
                                allModifiedVariables.add(variableName);
                                //individualDependencies.add(variableName);
                                continue;
                            }
                        }

                        /* TODO: lookup for prefixed/suffixed operations, array access and property access */

                        individualDependencies.add(variableName);
                    }
                }
            }
        };
    }
}
