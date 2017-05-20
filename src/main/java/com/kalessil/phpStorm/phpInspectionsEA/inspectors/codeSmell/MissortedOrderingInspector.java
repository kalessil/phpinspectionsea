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
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import java.util.Objects;

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
                final PhpModifier methodModifier = method.getModifier();

                if (!method.isStatic() && !method.isAbstract()) {
                    return;
                }

                final PhpModifierList methodModifierList = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);

                if ((methodModifierList == null)) {
                    return;
                }

                final StringBuilder currentOrdering = new StringBuilder(3);

                for (final LeafPsiElement element : PsiTreeUtil.findChildrenOfType(methodModifierList, LeafPsiElement.class)) {
                    if (element instanceof PsiWhiteSpace) {
                        continue;
                    }

                    currentOrdering.append(element.getText()).append(' ');
                }

                final String currentOrderingText = currentOrdering.toString().trim();

                if (Objects.equals(currentOrderingText, methodModifier.toString())) {
                    return;
                }

                problemsHolder.registerProblem(methodModifierList, String.format(message, currentOrderingText), ProblemHighlightType.WEAK_WARNING,
                                               new TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
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

            methodModifierList.getParent().addAfter(methodModifierList.getFirstChild(), methodModifierList);
            methodModifierList.getFirstChild().delete();
        }
    }
}
