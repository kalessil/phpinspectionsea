package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UnnecessaryElseFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * (c) Funivan <alotofall@gmail.com>
 */
public class UnnecessaryElseInspector extends BasePhpInspection {
    private static final String message = "Keyword else can be safely removed.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryElseInspection";
    }


    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpElse(Else elseStatement) {
                super.visitPhpElse(elseStatement);
                visitElseKeywords(elseStatement);
            }

            @Override
            public void visitPhpElseIf(ElseIf elseIfStatement) {
                super.visitPhpElseIf(elseIfStatement);
                visitElseKeywords(elseIfStatement);
            }

            private void visitElseKeywords(Statement elseStatement) {

                PhpPsiElement group = elseStatement.getPrevPsiSibling();

                if (group instanceof ElseIf) {
                    group = ((ElseIf) group).getStatement();
                }

                if (group == null) {
                    return;
                }

                PsiElement[] childrenStatements = group.getChildren();
                int childLen = childrenStatements.length;
                if (childLen == 0) {
                    return;
                }

                int key = childLen - 1;
                PsiElement lastChildElement = childrenStatements[key];
                if (lastChildElement == null) {
                    return;
                }

                // @todo According to the next statement elseif or else we can provide different fixers
                if (lastChildElement instanceof PhpReturn || lastChildElement instanceof PhpThrow) {
                    // return keyword before Else

                    LocalQuickFix fixer = hasBraces(elseStatement) ? new UnnecessaryElseFixer() : null;
                    holder.registerProblem(elseStatement.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixer);
                }

                if (lastChildElement instanceof Statement && lastChildElement.getFirstChild() instanceof PhpExit) {
                    // exit or die before Else
                    LocalQuickFix fixer = hasBraces(elseStatement) ? new UnnecessaryElseFixer() : null;
                    holder.registerProblem(elseStatement.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixer);
                }

            }
        };
    }

    private boolean hasBraces(PsiElement element) {
        PsiElement groupStatement = element.getLastChild();
        if (groupStatement == null) {
            return false;
        }
        PsiElement lastElement = groupStatement.getLastChild();
        return (lastElement != null && (lastElement.getNode().getElementType() == PhpTokenTypes.chRBRACE));

    }
}

