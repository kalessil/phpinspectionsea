package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

public class AssertNotCountStrategy {
    final static String message = "assertNotCount should be used instead";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (2 == params.length && (function.equals("assertNotSame") || function.equals("assertNotEquals"))) {
            /* analyze parameters which makes the call equal to assertCount */
            boolean isFirstCount = false;
            if (params[0] instanceof FunctionReference && !(params[0] instanceof MethodReference)) {
                final String referenceName = ((FunctionReference) params[0]).getName();
                isFirstCount = !StringUtil.isEmpty(referenceName) && referenceName.equals("count");
            }
            boolean isSecondCount = false;
            if (params[1] instanceof FunctionReference && !(params[1] instanceof MethodReference)) {
                final String referenceName = ((FunctionReference) params[1]).getName();
                isSecondCount = !StringUtil.isEmpty(referenceName) && referenceName.equals("count");
            }
                /* fire assertCount warning when needed */
            if ((isFirstCount && !isSecondCount) || (!isFirstCount && isSecondCount)) {
                final TheLocalFix fixer = new TheLocalFix(
                        isSecondCount ? params[0] : params[1],
                        ((FunctionReference) (isSecondCount ? params[1] : params[0])).getParameters()[0]
                );
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement expected;
        private PsiElement provided;

        TheLocalFix(@NotNull PsiElement expected, @NotNull PsiElement provided) {
            super();
            this.expected = expected;
            this.provided = provided;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertNotCount";
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
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "pattern(null, null)");
                replacement.getParameters()[0].replace(this.expected);
                replacement.getParameters()[1].replace(this.provided);

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotCount");
            }
        }
    }
}