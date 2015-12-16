package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessarySemicolonInspector extends BasePhpInspection {
    private static final String strProblemBadPractice = "Bad practice, using '{}' instead will reduce amount of possible bugs";
    private static final String strProblemColonOnly = "Unnecessary semicolon";

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
                    PsiElement parent = statement.getParent();
                    if (null != parent) {
                        IElementType declareCandidate = statement.getParent().getFirstChild().getNode().getElementType();
                        if (PhpTokenTypes.kwDECLARE == declareCandidate) {
                            return;
                        }

                        if (
                            parent instanceof DoWhile ||
                            parent instanceof While ||
                            parent instanceof For ||
                            parent instanceof ForeachStatement ||
                            parent instanceof If ||
                            parent instanceof ElseIf ||
                            parent instanceof Else
                        ) {
                            holder.registerProblem(statement, strProblemBadPractice, ProblemHighlightType.GENERIC_ERROR);
                            return;
                        }
                    }

                    holder.registerProblem(statement, strProblemColonOnly, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}

