package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UseSuggestedReplacementFixer implements LocalQuickFix {
    private static final String title = "Use suggested replacement";

    final private String expression;

    @NotNull
    @Override
    public String getName() {
        return title;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    protected UseSuggestedReplacementFixer(@NotNull String expression) {
        super();
        this.expression = expression;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement expression = descriptor.getPsiElement();
        if (expression != null && !project.isDisposed()) {
            final PsiElement replacement = PhpPsiElementFactory
                    .createPhpPsiFromText(project, ParenthesizedExpression.class, '(' + this.expression + ')')
                    .getArgument();
            if (replacement != null && !project.isDisposed()) {
                expression.replace(replacement);
            }
        }
    }
}
