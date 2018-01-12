package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

public class AssertNotCountStrategy {
    private final static String message = "assertNotCount should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 1 && (function.equals("assertNotSame") || function.equals("assertNotEquals"))) {
            /* analyze parameters which makes the call equal to assertNotCount */
            boolean isFirstCount = false;
            if (OpenapiTypesUtil.isFunctionReference(params[0])) {
                final String referenceName = ((FunctionReference) params[0]).getName();
                isFirstCount = referenceName != null && referenceName.equals("count");
            }
            boolean isSecondCount = false;
            if (OpenapiTypesUtil.isFunctionReference(params[1])) {
                final String referenceName = ((FunctionReference) params[1]).getName();
                isSecondCount = referenceName != null && referenceName.equals("count");
            }
            /* fire assertNotCount warning when needed */
            if ((isFirstCount && !isSecondCount) || (!isFirstCount && isSecondCount)) {
                final TheLocalFix fixer = new TheLocalFix(
                        isSecondCount ? params[0] : params[1],
                        ((FunctionReference) (isSecondCount ? params[1] : params[0])).getParameters()[0]
                );
                holder.registerProblem(reference, message, fixer);

                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> expected;
        final private SmartPsiElementPointer<PsiElement> provided;

        TheLocalFix(@NotNull PsiElement expected, @NotNull PsiElement provided) {
            super();
            final SmartPointerManager manager = SmartPointerManager.getInstance(expected.getProject());

            this.expected = manager.createSmartPsiElementPointer(expected);
            this.provided = manager.createSmartPsiElementPointer(provided);
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
            final PsiElement expected   = this.expected.getElement();
            final PsiElement provided   = this.provided.getElement();
            if (expression instanceof FunctionReference && expected != null && provided != null && !project.isDisposed()) {
                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 3 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(expected);
                replaceParams[1].replace(provided);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[2]);
                }

                final FunctionReference call = (FunctionReference) expression;
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotCount");
            }
        }
    }
}