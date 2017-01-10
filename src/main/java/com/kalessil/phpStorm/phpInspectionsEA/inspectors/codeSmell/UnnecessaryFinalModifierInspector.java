package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
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

public class UnnecessaryFinalModifierInspector extends BasePhpInspection {
    private static final String message = "Unnecessary final modifier (class is already final).";

    @NotNull
    public String getShortName() {
        return "UnnecessaryFinalModifierInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final PhpClass clazz      = method.getContainingClass();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == clazz || null == nameNode || !clazz.isFinal() || !method.isFinal()) {
                    return;
                }

                holder.registerProblem(nameNode, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Drop final modifier";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression && expression.getParent() instanceof Method) {
                final Method target   = (Method) expression.getParent();
                final String modifier = target.getModifier().toString().replace("final", "").replace("  ", "").trim();
                Method replacement    = PhpPsiElementFactory.createMethod(project, modifier + " function x(){}");
                target.getFirstChild().replace(replacement.getFirstChild());
            }
        }
    }

}
