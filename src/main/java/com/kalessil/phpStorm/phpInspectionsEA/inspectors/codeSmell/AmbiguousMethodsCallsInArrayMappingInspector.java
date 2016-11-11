package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AmbiguousMethodsCallsInArrayMappingInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Duplicated method calls should be moved to local variable";

    @NotNull
    public String getShortName() {
        return "AmbiguousMethodsCallsInArrayMappingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /**
             * @param foreach statement to inspect
             */
            public void visitPhpForeach(ForeachStatement foreach) {
                /* check if group statement used */
                GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (objGroupStatement == null) {
                    return;
                }

                for (PsiElement objStatement : objGroupStatement.getStatements()) {
                    if (objStatement.getFirstChild() instanceof AssignmentExpression) {
                        this.isStatementMatchesInspection((AssignmentExpression) objStatement.getFirstChild());
                    }
                }
            }

            /**
             * @param objAssignment to inspect
             */
            private void isStatementMatchesInspection(AssignmentExpression objAssignment) {
                if (
                    !(objAssignment.getValue() instanceof FunctionReference) ||
                    !(objAssignment.getVariable() instanceof ArrayAccessExpression)
                ) {
                    return;
                }

                FunctionReference objValueExpression = (FunctionReference) objAssignment.getValue();

                PhpPsiElement objContainer = objAssignment.getVariable();
                /* TODO: iterator for array access expression */
                while (objContainer instanceof ArrayAccessExpression) {
                    ArrayIndex objIndex = ((ArrayAccessExpression) objContainer).getIndex();
                    if (objIndex != null && objIndex.getValue() instanceof FunctionReference) {
                        FunctionReference objIndexExpression = (FunctionReference) objIndex.getValue();
                        if (PsiEquivalenceUtil.areElementsEquivalent(objIndexExpression, objValueExpression)) {
                            holder.registerProblem(objValueExpression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            break;
                        }
                    }

                    objContainer = ((ArrayAccessExpression) objContainer).getValue();
                }
            }
        };
    }
}
