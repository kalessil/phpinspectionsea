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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertFileExistsStrategy {
    final static String message = "assertFileExists should be used instead";

    static public void apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (1 == params.length && (function.equals("assertTrue") || function.equals("assertNotFalse"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof FunctionReference) {
                final FunctionReference call  = (FunctionReference) param;

                final PsiElement[] callParams = call.getParameters();
                final String callName         = call.getName();
                if (1 != callParams.length || StringUtil.isEmpty(callName) || !callName.equals("file_exists")) {
                    return;
                }

                final TheLocalFix fixer = new TheLocalFix(callParams[0]);
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);
            }
        }
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement fileName;

        TheLocalFix(@NotNull PsiElement fileName) {
            super();
            this.fileName = fileName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertFileExists";
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
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "pattern(null)");
                replacement.getParameters()[0].replace(this.fileName);

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertFileExists");
            }
        }
    }
}
