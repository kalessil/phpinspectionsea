package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy.ProtectedMembersOfFinalClassStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LowerAccessLevelInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "LowerAccessLevelInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpField(final Field field) {
                ProtectedMembersOfFinalClassStrategy.apply(field, problemsHolder);
            }

            @Override
            public void visitPhpMethod(final Method method) {
                ProtectedMembersOfFinalClassStrategy.apply(method, problemsHolder);
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> modifier;

        TheLocalFix(@NotNull final PsiElement modifierElement) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(modifierElement.getProject());

            modifier = manager.createSmartPsiElementPointer(modifierElement);
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare private";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement modifierElement     = modifier.getElement();
            final PsiElement modifierReplacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "private");

            if ((modifierElement == null) ||
                (modifierReplacement == null)) {
                return;
            }

            modifierElement.replace(modifierReplacement);
        }
    }
}
