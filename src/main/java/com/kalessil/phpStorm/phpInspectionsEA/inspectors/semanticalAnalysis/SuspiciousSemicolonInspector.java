package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;


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

public class SuspiciousSemicolonInspector extends BasePhpInspection {
    private static final String message = "Probably a bug, because ';' treated as body";

    @NotNull
    public String getShortName() {
        return "SuspiciousSemicolonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStatement(Statement statement) {
                if (0 == statement.getChildren().length) {
                    final PsiElement parent = statement.getParent();
                    if (null != parent) {
                        final IElementType declareCandidate = statement.getParent().getFirstChild().getNode().getElementType();
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
                            holder.registerProblem(statement, message, ProblemHighlightType.GENERIC_ERROR);
                            // return;
                        }
                    }
                }
            }
        };
    }
}

