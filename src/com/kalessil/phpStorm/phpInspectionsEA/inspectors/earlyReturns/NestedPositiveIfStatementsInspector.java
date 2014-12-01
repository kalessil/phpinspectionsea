package com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class NestedPositiveIfStatementsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "If statement can be merged into parent one.";

    @NotNull
    public String getDisplayName() {
        return "API: nested positive ifs";
    }

    @NotNull
    public String getShortName() {
        return "NestedPositiveIfStatementsInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /** meet pre-conditions */
                PsiElement objParent = ifStatement.getParent();
                if (!(objParent instanceof GroupStatement)) {
                    return;
                }
                objParent = objParent.getParent();
                if (!(objParent instanceof If)) {
                    return;
                }

                /** ensure parent if has no alternative branches */
                final boolean isParentHasAlternativeBranches = (
                    ((If) objParent).getElseBranch() != null ||
                    ((If) objParent).getElseIfBranches().length > 0
                );
                if (isParentHasAlternativeBranches) {
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
                for (PsiElement objStatement : ifStatement.getParent().getChildren()) {
                    if (!(objStatement instanceof PhpPsiElement)) {
                        continue;
                    }

                    ++countStatementsInParent;
                }
                if (countStatementsInParent > 1) {
                    return;
                }


                /** point on the issues */
                PhpPsiElement objIfCondition = ifStatement.getCondition();
                if (objIfCondition == null) {
                    return;
                }
                holder.registerProblem(objIfCondition, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}