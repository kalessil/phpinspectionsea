package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

abstract class BaseSameEqualsFunctionReferenceStrategy {
    @NotNull
    abstract protected String getRecommendedAssertionName();

    @NotNull
    abstract protected String getTargetFunctionName();

    abstract protected boolean isTargetFunctionProcessesGivenValue();

    final public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length < 2 || (!function.equals("assertSame") && !function.equals("assertEquals"))) {
            return false;
        }
        final String functionName = getTargetFunctionName();

        /* analyze parameters which makes the call equal to a higher level assert function */
        boolean isTargetFirst = false;
        if (OpenapiTypesUtil.isFunctionReference(params[0])) {
            final String referenceName = ((FunctionReference) params[0]).getName();
            isTargetFirst = !StringUtils.isEmpty(referenceName) && referenceName.equals(functionName);
        }
        boolean isTargetSecond = false;
        if (OpenapiTypesUtil.isFunctionReference(params[1])) {
            final String referenceName = ((FunctionReference) params[1]).getName();
            isTargetSecond = !StringUtils.isEmpty(referenceName) && referenceName.equals(functionName);
        }

        /* fire assertCount warning when needed */
        if ((isTargetFirst && !isTargetSecond) || (!isTargetFirst && isTargetSecond)) {
            final PsiElement[] processedParams = ((FunctionReference) (isTargetSecond ? params[1] : params[0])).getParameters();
            if (0 == processedParams.length) {
                return false;
            }

            final String replacement        = getRecommendedAssertionName();
            final PsiElement processedValue = processedParams[0];
            final PsiElement otherValue     = isTargetSecond ? params[0] : params[1];
            final boolean isProcessedFirst  = isTargetFunctionProcessesGivenValue();
            final TheLocalFix fixer  = new TheLocalFix(
                isProcessedFirst ? processedValue : otherValue,
                isProcessedFirst ? otherValue     : processedValue,
                replacement
            );

            final String message = replacement + " should be used instead.";
            holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

            return true;
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> expected;
        final private SmartPsiElementPointer<PsiElement> provided;
        final private String replacement;

        TheLocalFix(@NotNull PsiElement expected, @NotNull PsiElement provided, @NotNull String replacement) {
            super();
            SmartPointerManager manager = SmartPointerManager.getInstance(expected.getProject());

            this.expected    = manager.createSmartPsiElementPointer(expected);
            this.provided    = manager.createSmartPsiElementPointer(provided);
            this.replacement = replacement;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested assertion";
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
            if (null != expected && null != provided && expression instanceof FunctionReference) {
                final FunctionReference reference = (FunctionReference) expression;

                /* build-up a pattern */
                final PsiElement[] params           = reference.getParameters();
                final boolean hasCustomMessage      = 3 == params.length;
                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();

                /* inject parameters */
                replaceParams[0].replace(expected);
                replaceParams[1].replace(provided);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[2]);
                }

                /* replace tree nodes */
                //noinspection ConstantConditions - we areworking with hard-coded structures
                reference.getParameterList().replace(replacement.getParameterList());
                reference.handleElementRename(this.replacement);
            }
        }
    }
}