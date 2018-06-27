package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryFinalModifierInspector extends BasePhpInspection {
    private static final String message = "Unnecessary final modifier.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryFinalModifierInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (method.isFinal()) {
                    final PhpClass clazz = method.getContainingClass();
                    if (
                        (clazz != null && clazz.isFinal()) ||
                        (!method.getName().startsWith("__") && method.getAccess().isPrivate())
                    ) {
                        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                        if (nameNode != null) {
                            holder.registerProblem(nameNode, message, new TheLocalFix());
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Drop final modifier";

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
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression && expression.getParent() instanceof Method) {
                final Method target    = (Method) expression.getParent();
                final String modifiers = target.getModifier().toString().replace("final", "").replace("  ", "").trim();
                final Method donor     = PhpPsiElementFactory.createMethod(project, modifiers + " function x(){}");
                target.getFirstChild().replace(donor.getFirstChild());
            }
        }
    }

}
