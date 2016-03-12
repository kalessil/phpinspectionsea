package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessarySemicolonInspector extends BasePhpInspection {
    private static final String message = "Unnecessary semicolon";

    @NotNull
    public String getShortName() {
        return "UnnecessarySemicolonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStatement(Statement statement) {
                if (0 == statement.getChildren().length) {
                    holder.registerProblem(statement, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}

