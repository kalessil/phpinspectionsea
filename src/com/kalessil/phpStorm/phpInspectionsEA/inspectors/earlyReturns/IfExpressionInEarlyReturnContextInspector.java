package com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class IfExpressionInEarlyReturnContextInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Consider refactoring the statement, " +
            "so it follows early return approach.";

    @NotNull
    public String getDisplayName() {
        return "Code smell: early returns can be used";
    }

    @NotNull
    public String getShortName() {
        return "IfExpressionInEarlyReturnContextInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                if (!(ifStatement.getParent() instanceof GroupStatement)) {
                    return;
                }

                /** ensure it's right context */
                GroupStatement objGroupExpression = (GroupStatement) ifStatement.getParent();
                final boolean isTargetContext = (
                    objGroupExpression.getParent() instanceof ForeachStatement ||
                    objGroupExpression.getParent() instanceof Function ||
                    objGroupExpression.getParent() instanceof For
                );
                if (!isTargetContext) {
                    return;
                }


                /** ensure if has no alternative branches as well */
                final boolean isIfHasAlternativeBranches = (
                    ifStatement.getElseBranch() != null ||
                    ifStatement.getElseIfBranches().length > 0
                );
                if (isIfHasAlternativeBranches) {
                    return;
                }


                /** ensure that if is single expression in group */
                int countStatementsInParent = 0;
                for (PsiElement objStatement : objGroupExpression.getChildren()) {
                    if (!(objStatement instanceof PhpPsiElement)) {
                        continue;
                    }

                    ++countStatementsInParent;
                }
                if (countStatementsInParent > 1) {
                    return;
                }


                holder.registerProblem(ifStatement.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}