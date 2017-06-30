package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MisorderedModifiersInspector extends BasePhpInspection {
    private static final String message = "Modifiers are misordered (according to PSRs)";

    @NotNull
    public String getShortName() {
        return "MisorderedModifiersInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(final Method method) {
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

                final String modifiersOriginalOrdering = describeOriginalOrdering(methodModifiers, LeafElement::getText).toLowerCase();
                final String modifiersExpectedOrdering =
                    describeExpectedOrdering(modifiersOriginalOrdering, methodModifiers.size(),
                                             "final", "abstract",
                                             "public", "protected", "private",
                                             "static");

                if (modifiersOriginalOrdering.equals(modifiersExpectedOrdering)) {
                    return;
                }

                problemsHolder.registerProblem(methodModifierList, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(modifiersExpectedOrdering));
            }

            private String describeOriginalOrdering(final Collection<LeafPsiElement> methodModifiers, final Function<LeafPsiElement, String> modifierText) {
                final StringBuilder describerBuilder = new StringBuilder(methodModifiers.size());

                for (final LeafPsiElement methodModifier : methodModifiers) {
                    describerBuilder.append(modifierText.apply(methodModifier)).append(' ');
                }

                return describerBuilder.toString().trim();
            }

            private String describeExpectedOrdering(final String modifiersOriginalOrdering, final int modifiersSize, @NotNull final String... expectedOrderingKeywords) {
                final StringBuilder describerBuilder = new StringBuilder(modifiersSize);

                for (final String expectedOrderingKeyword : expectedOrderingKeywords) {
                    if (modifiersOriginalOrdering.contains(expectedOrderingKeyword)) {
                        describerBuilder.append(expectedOrderingKeyword).append(' ');
                    }
                }

                return describerBuilder.toString().trim();
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private final String methodModifiers;

        private TheLocalFix(final String methodModifiers) {
            this.methodModifiers = methodModifiers;
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

            final Method replacement = PhpPsiElementFactory.createMethod(project, methodModifiers + " function x();");
            methodModifierList.replace(replacement.getFirstChild());
        }
    }
}
