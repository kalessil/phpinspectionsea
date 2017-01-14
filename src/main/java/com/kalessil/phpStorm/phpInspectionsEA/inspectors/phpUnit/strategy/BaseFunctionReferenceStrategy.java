package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

abstract public class BaseFunctionReferenceStrategy {
    @NotNull
    abstract protected String getRecommendedAssertionName();

    @NotNull
    abstract protected String getTargetFunctionName();

    public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length < 2 || (!function.equals("assertSame") && !function.equals("assertEquals"))) {
            return false;
        }
        final String functionName = getTargetFunctionName();

        /* analyze parameters which makes the call equal to a higher level assert function */
        boolean isTargetFirst = false;
        if (params[0] instanceof FunctionReference) {
            final String referenceName = ((FunctionReference) params[0]).getName();
            isTargetFirst = !StringUtil.isEmpty(referenceName) && referenceName.equals(functionName);
        }
        boolean isTargetSecond = false;
        if (params[1] instanceof FunctionReference) {
            final String referenceName = ((FunctionReference) params[1]).getName();
            isTargetSecond = !StringUtil.isEmpty(referenceName) && referenceName.equals(functionName);
        }

        /* fire assertCount warning when needed */
        if ((isTargetFirst && !isTargetSecond) || (!isTargetFirst && isTargetSecond)) {
            final String message = getRecommendedAssertionName() + " should be used instead.";
            holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);

            return true;
        }

        return false;
    }
}