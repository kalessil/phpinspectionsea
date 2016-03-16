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

public class AssertFalseStrategy {
    final static String message = "assertFalse should be used instead";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 1 && function.equals("assertSame")) {
            /* analyze parameters which makes the call equal to assertFalse */
            boolean isFirstFalse = false;
            if (params[0] instanceof ConstantReference) {
                final String constantName = ((ConstantReference) params[0]).getName();
                isFirstFalse = !StringUtil.isEmpty(constantName) && constantName.equals("false");
            }
            boolean isSecondFalse = false;
            if (params[1] instanceof ConstantReference) {
                final String referenceName = ((ConstantReference) params[1]).getName();
                isSecondFalse = !StringUtil.isEmpty(referenceName) && referenceName.equals("false");
            }

            /* fire assertFalse warning when needed */
            if ((isFirstFalse && !isSecondFalse) || (!isFirstFalse && isSecondFalse)) {
                final TheLocalFix fixer = new TheLocalFix(isFirstFalse ? params[1] : params[0]);
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
            return "Use ::assertFalse";
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
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(this.value);
                if (hasCustomMessage) {
                    replaceParams[1].replace(params[2]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertFalse");
            }

            /* release a tree node reference */
            this.value = null;
        }
    }
}
