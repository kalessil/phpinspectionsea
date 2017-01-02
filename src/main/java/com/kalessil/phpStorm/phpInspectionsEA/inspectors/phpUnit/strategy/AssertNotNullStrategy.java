package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class AssertNotNullStrategy {
    private final static String message = "assertNotNull should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 1 && function.equals("assertNotSame")) {
            /* analyze parameters which makes the call equal to assertNotNull */
            final boolean isFirstNull  = PhpLanguageUtil.isNull(params[0]);
            final boolean isSecondNull = PhpLanguageUtil.isNull(params[1]);

            /* fire assertNotNull warning when needed */
            if ((isFirstNull && !isSecondNull) || (!isFirstNull && isSecondNull)) {
                final TheLocalFix fixer = new TheLocalFix(isFirstNull ? params[1] : params[0]);
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        private SmartPsiElementPointer<PsiElement> value;

        TheLocalFix(@NotNull PsiElement value) {
            super();
            SmartPointerManager manager =  SmartPointerManager.getInstance(value.getProject());

            this.value = manager.createSmartPsiElementPointer(value);
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertNotNull";
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

                final PsiElement value = this.value.getElement();
                if (null == value) {
                    return;
                }
                replaceParams[0].replace(value);
                if (hasCustomMessage) {
                    replaceParams[1].replace(params[2]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotNull");
            }
        }
    }
}
