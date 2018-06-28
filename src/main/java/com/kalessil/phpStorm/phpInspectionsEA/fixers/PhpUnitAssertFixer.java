package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PhpUnitAssertFixer implements LocalQuickFix {
    private static final String title = "Use suggested assertion instead";

    private final String suggestedAssertion;
    private final String[] arguments;

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

    public PhpUnitAssertFixer (@NotNull String suggestedAssertion, @NotNull String[] arguments) {
        this.suggestedAssertion = suggestedAssertion;
        this.arguments          = arguments;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement expression = descriptor.getPsiElement();
        if (expression instanceof MethodReference && !project.isDisposed()) {
            final MethodReference assertion = (MethodReference) expression;
            final String pattern            = String.format("pattern(%s)", String.join(", ", this.arguments));
            final PsiElement donor          = PhpPsiElementFactory.createFunctionReference(project, pattern).getParameterList();
            final PsiElement socket         = assertion.getParameterList();
            if (donor != null && socket != null) {
                socket.replace(donor);
                assertion.handleElementRename(this.suggestedAssertion);
            }
        }
    }
}
