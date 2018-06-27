package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

final public class DropMethodFix implements LocalQuickFix {
    private static final String title = "Drop the method";

    @NotNull
    @Override
    public String getName() {
        return title;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return title;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement expression = descriptor.getPsiElement().getParent();
        if (expression instanceof Method && !project.isDisposed()) {
            /* delete preceding PhpDoc */
            final PhpPsiElement previous = ((Method) expression).getPrevPsiSibling();
            if (previous instanceof PhpDocComment) {
                previous.delete();
            }

            /* delete space after the method */
            final PsiElement nextExpression = expression.getNextSibling();
            if (nextExpression instanceof PsiWhiteSpace) {
                nextExpression.delete();
            }

            /* delete the method itself */
            expression.delete();
        }
    }
}
