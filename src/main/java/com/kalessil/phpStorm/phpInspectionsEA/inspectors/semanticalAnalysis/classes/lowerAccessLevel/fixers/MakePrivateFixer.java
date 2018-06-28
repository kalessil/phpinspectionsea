package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MakePrivateFixer implements LocalQuickFix {
    private static final String title = "Make it private";

    private final SmartPsiElementPointer<PsiElement> modifier;

    public MakePrivateFixer(@NotNull final PsiElement modifierElement) {
        super();

        modifier = SmartPointerManager.getInstance(modifierElement.getProject()).createSmartPsiElementPointer(modifierElement);
    }

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
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        final PsiElement element     = modifier.getElement();
        final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "private");
        if (element != null && replacement != null) {
            element.replace(replacement);
        }
    }
}
