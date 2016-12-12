package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SuspiciousAssignmentsInspector extends BasePhpInspection {
    private static final String message = "Overrides value from a preceding case (perhaps break is missing there)";

    @NotNull
    public String getShortName() {
        return "SuspiciousAssignmentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpSwitch(PhpSwitch switchStatement) {
                final List<PsiElement> written = new ArrayList<>();
                for (PhpCase oneCase : switchStatement.getAllCases()) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(oneCase);
                    if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        continue;
                    }

                    for (PsiElement expression : body.getChildren()) {
                        /* get expression from '...;' constructs */
                        if (expression instanceof Statement) {
                            expression = expression.getFirstChild();
                        }

                        if (expression instanceof MultiassignmentExpression) {
                            for (PsiElement variable : ((MultiassignmentExpression) expression).getVariables()) {
                                /* HashSet is not working here, hence manual checks */
                                boolean isOverridden = false;
                                for (PsiElement writtenVariable : written) {
                                    if (PsiEquivalenceUtil.areElementsEquivalent(writtenVariable, variable)) {
                                        isOverridden = true;
                                        holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);

                                        break;
                                    }
                                }
                                if (!isOverridden) {
                                    written.add(variable);
                                }
                            }
                            continue;
                        }

                        if (OpenapiTypesUtil.isAssignment(expression)) {
                            final PsiElement variable = ((AssignmentExpression) expression).getVariable();
                            if (null != variable) {
                                /* HashSet is not working here, hence manual checks */
                                boolean isOverridden = false;
                                for (PsiElement writtenVariable : written) {
                                    if (PsiEquivalenceUtil.areElementsEquivalent(writtenVariable, variable)) {
                                        isOverridden = true;
                                        holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);

                                        break;
                                    }
                                }
                                if (!isOverridden) {
                                    written.add(variable);
                                }
                            }
                            // continue;
                        }
                    }

                    /* once break met, clean the written expressions out */
                    final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                    if (last instanceof PhpBreak) {
                        written.clear();
                        // continue;
                    }
                }
                written.clear();
            }
        };
    }
}
