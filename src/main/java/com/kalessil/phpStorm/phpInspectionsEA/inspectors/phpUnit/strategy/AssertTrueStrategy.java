package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AssertTrueStrategy {

    private final static Set<String> targetConstants       = new HashSet<>();
    private final static Map<String, String> targetMapping = new HashMap<>();
    static {
        targetConstants.add("null");
        targetConstants.add("true");
        targetConstants.add("false");

        targetMapping.put("assertSame",    "assert%s");
        targetMapping.put("assertNotSame", "assertNot%s");
    }

    private final static String message = "assertTrue should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 1 && function.equals("assertSame")) {
            /* analyze parameters which makes the call equal to assertTrue */
            final boolean isFirstTrue  = PhpLanguageUtil.isTrue(params[0]);
            final boolean isSecondTrue = PhpLanguageUtil.isTrue(params[1]);

            /* fire assertTrue warning when needed */
            if ((isFirstTrue && !isSecondTrue) || (!isFirstTrue && isSecondTrue)) {
                final TheLocalFix fixer = new TheLocalFix(isFirstTrue ? params[1] : params[0]);
                holder.registerProblem(reference, message, fixer);

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
            return "Use ::assertTrue";
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
                call.handleElementRename("assertTrue");
            }

            /* release a tree node reference */
            this.value = null;
        }
    }
}
