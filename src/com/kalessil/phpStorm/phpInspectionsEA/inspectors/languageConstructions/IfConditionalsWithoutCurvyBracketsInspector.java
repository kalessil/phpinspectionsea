package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class IfConditionalsWithoutCurvyBracketsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Wrap the conditional body with curvy brackets";

    @NotNull
    public String getDisplayName() {
        return "API: if/else/elseif curvy brackets";
    }

    @NotNull
    public String getShortName() {
        return "IfConditionalsWithoutCurvyBracketsInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                this.checkBrackets(ifStatement);
            }

            public void visitPhpElseIf(ElseIf elseIfStatement) {
                this.checkBrackets(elseIfStatement);
            }

            public void visitPhpElse(Else elseStatement) {
                this.checkBrackets(elseStatement);
            }

            private void checkBrackets(PhpPsiElement objConditional) {
                boolean hasGroupStatement = false;
                for (PsiElement objChild : objConditional.getChildren()) {
                    if (objChild instanceof GroupStatement) {
                        hasGroupStatement = true;
                        break;
                    }
                }

                if (hasGroupStatement) {
                    return;
                }

                holder.registerProblem(objConditional.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
