package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

public class AssertNotTrueStrategy {
    final static String message = "assertNotTrue should be used instead";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 1 && function.equals("assertNotSame")) {
            /* analyze parameters which makes the call equal to assertNotTrue */
            boolean isFirstTrue = false;
            if (params[0] instanceof ConstantReference) {
                final String constantName = ((ConstantReference) params[0]).getName();
                isFirstTrue = !StringUtil.isEmpty(constantName) && constantName.equals("true");
            }
            boolean isSecondTrue = false;
            if (params[1] instanceof ConstantReference) {
                final String referenceName = ((ConstantReference) params[1]).getName();
                isSecondTrue = !StringUtil.isEmpty(referenceName) && referenceName.equals("true");
            }

            /* fire assertNotTrue warning when needed */
            if ((isFirstTrue && !isSecondTrue) || (!isFirstTrue && isSecondTrue)) {
                final TheLocalFix fixer = new TheLocalFix(isFirstTrue ? params[1] : params[0]);
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement value;

        TheLocalFix(@NotNull PsiElement value) {
            super();
            this.value = value;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertNotTrue";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 3 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null)" : "pattern(null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                replacement.getParameters()[0].replace(this.value);
                if (hasCustomMessage) {
                    replacement.getParameters()[1].replace(params[2]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotTrue");
            }
        }
    }
}
