package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import java.util.List;
import java.util.stream.Collectors;

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

public class MissortedOrderingInspector extends BasePhpInspection {
    private static final String message = "Missorted modifiers '%s'";

    @NotNull
    public String getShortName() {
        return "MissortedOrderingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(final Method method) {
                final PhpModifier methodModifierProcessed = method.getModifier();

                if (!method.isStatic() && !method.isAbstract()) {
                    return;
                }

                final PhpModifierList methodModifierList = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);

                if (methodModifierList == null) {
                    return;
                }

                final List<LeafPsiElement> methodModifiers =
                    PsiTreeUtil.findChildrenOfType(methodModifierList, LeafPsiElement.class).stream()
                               .filter(element -> !(element instanceof PsiWhiteSpace))
                               .collect(Collectors.toList());

                if (methodModifiers.size() < 2) {
                    return;
                }

                final PhpClass methodClass = method.getContainingClass();

                if (methodClass == null) {
                    return;
                }

                final StringBuilder methodModifiersBuilder = new StringBuilder(methodModifiers.size());

                for (final LeafPsiElement methodModifier : methodModifiers) {
                    methodModifiersBuilder.append(methodModifier.getText()).append(' ');
                }

                final String methodModifiersText    = methodModifiersBuilder.toString().trim();
                final String methodModifiersSubject = methodModifierProcessed.toString();

                if (methodModifiersText.equals(methodModifiersSubject)) {
                    return;
                }

                problemsHolder.registerProblem(methodModifierList, String.format(message, methodModifiersText), ProblemHighlightType.WEAK_WARNING,
                                               new TheLocalFix(methodModifiers));
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private final List<LeafPsiElement> methodModifiers;

        private TheLocalFix(final List<LeafPsiElement> methodModifiers) {
            this.methodModifiers = methodModifiers;
        }

        private void reallocateModifier(@NotNull final PsiElement methodModifierList, @NotNull final String... modifierTypes) {
            for (final LeafPsiElement methodModifier : methodModifiers) {
                for (final String modifierType : modifierTypes) {
                    if (methodModifier.getText().equals(modifierType)) {
                        methodModifierList.getParent().addBefore(methodModifier, methodModifierList);
                        methodModifier.delete();
                    }
                }
            }
        }

        @NotNull
        @Override
        public String getName() {
            return "Sort modifiers";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement methodModifierList = descriptor.getPsiElement();

            reallocateModifier(methodModifierList, "abstract");
            reallocateModifier(methodModifierList, "public", "protected", "private");
            reallocateModifier(methodModifierList, "static");
        }
    }
}
