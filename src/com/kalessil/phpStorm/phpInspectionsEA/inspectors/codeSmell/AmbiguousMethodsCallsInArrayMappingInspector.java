package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
             * @param foreach
             */
            public void visitPhpForeach(ForeachStatement foreach) {
                /** check if group statement used */
                GroupStatement objForeachBody = null;
                for (PsiElement objChild: foreach.getChildren()) {
                    if (objChild instanceof GroupStatement) {
                        objForeachBody = (GroupStatement) objChild;
                        break;
                    }
                }
                if (objForeachBody == null) {
                    return;
                }

                for (PsiElement objStatement : objForeachBody.getStatements()) {
                    if (!(objStatement.getFirstChild() instanceof AssignmentExpression)) {
                        continue;
                    }

                    this.isStatementMatchesInspection((AssignmentExpression) objStatement.getFirstChild());
                }
            }

            /**
             * @param objAssignment
             */
            private void isStatementMatchesInspection(AssignmentExpression objAssignment) {
                if (
                    !(objAssignment.getValue() instanceof MethodReference) ||
                    !(objAssignment.getVariable() instanceof ArrayAccessExpression)
                ) {
                    return;
                }

                MethodReference objValueExpression = (MethodReference) objAssignment.getValue();
                //holder.registerProblem(objValueExpression, "-pattern-", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                ArrayIndex objIndex = null;
                MethodReference objIndexExpression = null;

                PhpPsiElement objContainer = objAssignment.getVariable();
                while (objContainer instanceof ArrayAccessExpression) {
                    objIndex = ((ArrayAccessExpression) objContainer).getIndex();
                    if (objIndex != null && objIndex.getValue() instanceof MethodReference) {
                        objIndexExpression = (MethodReference) objIndex.getValue();

                        //if (objIndexExpression.textMatches(objValueExpression)) {
                        //if (objIndexExpression.isEquivalentTo(objValueExpression)) {
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