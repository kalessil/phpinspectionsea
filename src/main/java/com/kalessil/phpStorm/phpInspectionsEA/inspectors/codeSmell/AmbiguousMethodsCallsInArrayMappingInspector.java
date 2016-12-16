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
    private static final String message = "Duplicated method calls should be moved to local variable";

    @NotNull
    public String getShortName() {
        return "AmbiguousMethodsCallsInArrayMappingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (null != body) {
                    /* do net search with utils, as we can have nested loops */
                    for (PsiElement expression : body.getStatements()) {
                        final PsiElement assignCandidate = expression.getFirstChild();
                        if (assignCandidate instanceof AssignmentExpression) {
                            this.isStatementMatchesInspection((AssignmentExpression) assignCandidate);
                        }
                    }
                }
            }

            private void isStatementMatchesInspection(@NotNull AssignmentExpression assignment) {
                final PsiElement value    = assignment.getValue();
                final PsiElement variable = assignment.getVariable();
                if (!(value instanceof FunctionReference) || !(variable instanceof ArrayAccessExpression)) {
                    return;
                }

                PhpPsiElement container = (PhpPsiElement) variable;
                while (container instanceof ArrayAccessExpression) {
                    final ArrayAccessExpression arrayAccess = (ArrayAccessExpression) container;
                    final ArrayIndex indexContainer         = arrayAccess.getIndex();
                    final PsiElement index                  = null == indexContainer ? null : indexContainer.getValue();
                    if (index instanceof FunctionReference && PsiEquivalenceUtil.areElementsEquivalent(index, value)) {
                        holder.registerProblem(value, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        break;
                    }

                    container = arrayAccess.getValue();
                }
            }
        };
    }
}
