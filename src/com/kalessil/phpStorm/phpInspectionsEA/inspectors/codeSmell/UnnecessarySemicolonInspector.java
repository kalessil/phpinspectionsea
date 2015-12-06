package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessarySemicolonInspector extends BasePhpInspection {
    private static final String strProblemColonOnly = "Unnecessary semicolon";

    @NotNull
    public String getShortName() {
        return "UnnecessarySemicolonInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStatement(Statement statement) {
                if (
                    statement instanceof StatementImpl &&
                    1 == statement.getChildren().length && statement.getFirstChild() instanceof LeafPsiElement
                ) {
                    holder.registerProblem(statement, strProblemColonOnly, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}

