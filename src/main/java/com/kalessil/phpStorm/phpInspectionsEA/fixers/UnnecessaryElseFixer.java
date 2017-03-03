package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

/**
 * (c) Funivan <alotofall@gmail.com>
 */
public class UnnecessaryElseFixer implements LocalQuickFix {

    @NotNull
    @Override
    public String getName() {
        return "Remove redundant else keyword";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }


    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element = descriptor.getPsiElement();

        if (null == element) {
            return;
        }
        final PsiElement expression = element.getParent();

        // handle `else if`: Just delete else
        if (expression instanceof Else && expression.getLastChild() instanceof If) {
            element.delete();
            return;
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
            return;
        }

        //@todo check if we should fire exception. This lines should be never executed
    }

}
