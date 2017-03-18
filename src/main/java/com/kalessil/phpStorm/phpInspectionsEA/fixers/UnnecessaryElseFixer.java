package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

public class UnnecessaryElseFixer implements LocalQuickFix {
    @NotNull
    @Override
    public String getName() {
        return "Split the workflows";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element    = descriptor.getPsiElement();
        final PsiElement expression = null == element ? null : element.getParent();
        final PsiElement newline    = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, "\n");
        if (null == expression || null == newline) {
            return;
        }

        if (expression instanceof Else) {
            final Else elseStatement        = (Else) expression;
            final If parentIfExpression     = (If) expression.getParent();
            final PsiElement parentIfHolder = parentIfExpression.getParent();

            if (elseStatement.getFirstPsiChild() instanceof If) { /* handle 'else if' */
                final If nestedIfCopy = (If) elseStatement.getFirstPsiChild().copy();
                elseStatement.delete();

                parentIfHolder.addAfter(nestedIfCopy, parentIfExpression);
                parentIfHolder.addAfter(newline, parentIfExpression);

                return;
            }

            if (elseStatement.getFirstPsiChild() instanceof GroupStatement) { /* handle 'else {} ' */
                final GroupStatement elseBodyCopy = (GroupStatement) elseStatement.getFirstPsiChild().copy();
                elseStatement.delete();

                final PsiElement startBracket = elseBodyCopy.getFirstChild();
                final PsiElement endBracket   = elseBodyCopy.getLastChild();
                PsiElement last               = endBracket.getPrevSibling();
                if (last instanceof PsiWhiteSpace) {
                    last = last.getPrevSibling();
                }
                while (null != last) {
                    if (last != endBracket && last != startBracket) {
                        parentIfHolder.addAfter(last, parentIfExpression);
                    }
                    last = last.getPrevSibling();
                }

                parentIfHolder.addAfter(newline, parentIfExpression);

                return;
            }
        }

        // handle `elseif`: replace elseif with if
        if (expression instanceof ElseIf) {
            Statement anIf = PhpPsiElementFactory.createStatement(project, "if");
            element.replace(anIf);
        }

    }

}
