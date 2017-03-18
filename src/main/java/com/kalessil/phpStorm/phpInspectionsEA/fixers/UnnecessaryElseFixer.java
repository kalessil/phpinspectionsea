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
        if (null == expression) {
            return;
        }

        if (expression instanceof Else) {
            final Else elseStatement = (Else) expression;

            if (elseStatement.getFirstPsiChild() instanceof If) { /* handle 'else if' */
                final If nestedIfCopy       = (If) elseStatement.getFirstPsiChild().copy();
                final If parentIfExpression = (If) expression.getParent();
                elseStatement.delete();

                final PsiElement newline = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, "\n");
                parentIfExpression.getParent().addAfter(nestedIfCopy, parentIfExpression);
                if (null != newline) {
                    parentIfExpression.getParent().addAfter(newline, parentIfExpression);
                }
                return;
            }
        }


        // handle `else`: remove braces and else keyword
        if (expression instanceof Else) {
            PhpPsiElement statement = ((Else) expression).getStatement();
            if (statement != null) {
                PsiElement firstChild = statement.getFirstChild();
                if (firstChild.getNode().getElementType() == PhpTokenTypes.chLBRACE) {
                    firstChild.delete();

                    PsiElement lastChild = statement.getLastChild();
                    if (lastChild.getNode().getElementType() == PhpTokenTypes.chRBRACE) {
                        lastChild.delete();
                    }

                    element.delete();
                    return;
                }


            }
        }

        // handle `elseif`: replace elseif with if
        if (expression instanceof ElseIf) {
            Statement anIf = PhpPsiElementFactory.createStatement(project, "if");
            element.replace(anIf);
        }

    }

}
