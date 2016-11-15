package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertNotEmptyStrategy {
    private final static String message = "assertNotEmpty should be used instead";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 0 && (function.equals("assertFalse") || function.equals("assertNotTrue"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof PhpEmpty) {
                final PsiElement[] variables = ((PhpEmpty) param).getVariables();
                if (1 != variables.length) {
                    return false;
                }

                final TheLocalFix fixer = new TheLocalFix(variables[0]);
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement argument;

        TheLocalFix(@NotNull PsiElement fileName) {
            super();
            this.argument = fileName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertNotEmpty";
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
                final boolean hasCustomMessage = 2 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null)" : "pattern(null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(this.argument);
                if (hasCustomMessage) {
                    replaceParams[1].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotEmpty");
            }

            /* release a tree node reference */
            this.argument = null;
        }
    }
}
