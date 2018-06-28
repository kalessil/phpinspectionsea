package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private static final List<String> standardOrder = new ArrayList<>();
    static {
        standardOrder.add("final");
        standardOrder.add("abstract");
        standardOrder.add("public");
        standardOrder.add("protected");
        standardOrder.add("private");
        standardOrder.add("static");
    }

    @NotNull
    public String getShortName() {
        return "MisorderedModifiersInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (method.isStatic() || method.isAbstract() || method.isFinal()) {
                    final PhpModifierList modifiersNode  = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);
                    final List<LeafPsiElement> modifiers = PsiTreeUtil.findChildrenOfType(modifiersNode, LeafPsiElement.class).stream()
                                .filter(element -> !(element instanceof PsiWhiteSpace))
                                .collect(Collectors.toList());
                    if (modifiersNode != null && modifiers.size() >= 2) {
                        final String original = this.getOriginalOrder(modifiers);
                        final String expected = this.getExpectedOrder(original, standardOrder);
                        if (!original.equals(expected)) {
                            problemsHolder.registerProblem(modifiersNode, message, new TheLocalFix(expected));
                        }
                    }
                }
            }

            @NotNull
            private String getOriginalOrder(@NotNull Collection<LeafPsiElement> original) {
                return original.stream().map(LeafElement::getText).collect(Collectors.joining(" ")).toLowerCase();
            }

            @NotNull
            private String getExpectedOrder(@NotNull String original, @NotNull Collection<String> expected) {
                return expected.stream().filter(original::contains).collect(Collectors.joining(" "));
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Sort modifiers";

        private final String modifiers;

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

        private TheLocalFix(@NotNull String modifiers) {
            this.modifiers = modifiers;
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null) {
                target.replace(PhpPsiElementFactory.createMethod(project, modifiers + " function x();").getFirstChild());
            }
        }
    }
}
