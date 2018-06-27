package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

final public class AssertBoolInvertedStrategy {
    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 0 && (function.equals("assertTrue") || function.equals("assertFalse"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof UnaryExpression) {
                final UnaryExpression not = (UnaryExpression) param;
                if (!OpenapiTypesUtil.is(not.getOperation(), PhpTokenTypes.opNOT)) {
                    return false;
                }

                final PsiElement invertedParam = ExpressionSemanticUtil.getExpressionTroughParenthesis(not.getValue());
                if (null == invertedParam) {
                    return false;
                }

                final String replacementMethod = function.equals("assertTrue") ? "assertNotTrue" : "assertNotFalse";
                holder.registerProblem(
                        reference,
                        String.format(messagePattern, replacementMethod),
                        new TheLocalFix(replacementMethod, invertedParam)
                );
                return true;
            }
        }

        return false;
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use suggested assertion instead";

        final private String replacementFunction;
        final private SmartPsiElementPointer<PsiElement> argument;

        TheLocalFix(@NotNull String replacementFunction, @NotNull PsiElement argument) {
            super();

            this.replacementFunction = replacementFunction;
            this.argument            = SmartPointerManager.getInstance(argument.getProject()).createSmartPsiElementPointer(argument);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title + " (assert not boolean)";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement argument   = this.argument.getElement();
            if (expression instanceof FunctionReference && argument != null && !project.isDisposed()) {
                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 2 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null)" : "pattern(null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(argument);
                if (hasCustomMessage) {
                    replaceParams[1].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename(this.replacementFunction);
            }
        }
    }
}
