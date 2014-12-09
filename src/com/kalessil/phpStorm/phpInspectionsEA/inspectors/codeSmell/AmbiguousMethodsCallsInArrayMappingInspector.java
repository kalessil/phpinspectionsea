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
    private static final String strProblemDescription = "This method call is duplicate in assignment expression";

    @NotNull
    public String getDisplayName() {
        return "Clean code: non optimized arrays mapping";
    }

    @NotNull
    public String getShortName() {
        return "AmbiguousMethodsCallsInArrayMappingInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /**
             * @param foreach statement to inspect
             */
            public void visitPhpForeach(ForeachStatement foreach) {
                /** check if group statement used */
                GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (objGroupStatement == null) {
                    return;
                }

                for (PsiElement objStatement : objGroupStatement.getStatements()) {
                    if (!(objStatement.getFirstChild() instanceof AssignmentExpression)) {
                        continue;
                    }

                    this.isStatementMatchesInspection((AssignmentExpression) objStatement.getFirstChild());
                }
            }

            /**
             * @param objAssignment to inspect
             */
            private void isStatementMatchesInspection(AssignmentExpression objAssignment) {
                if (
                    !(objAssignment.getValue() instanceof MethodReference) ||
                    !(objAssignment.getVariable() instanceof ArrayAccessExpression)
                ) {
                    return;
                }

                MethodReference objValueExpression = (MethodReference) objAssignment.getValue();

                ArrayIndex objIndex;
                MethodReference objIndexExpression;

                PhpPsiElement objContainer = objAssignment.getVariable();
                while (objContainer instanceof ArrayAccessExpression) {
                    objIndex = ((ArrayAccessExpression) objContainer).getIndex();
                    if (objIndex != null && objIndex.getValue() instanceof MethodReference) {
                        objIndexExpression = (MethodReference) objIndex.getValue();

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