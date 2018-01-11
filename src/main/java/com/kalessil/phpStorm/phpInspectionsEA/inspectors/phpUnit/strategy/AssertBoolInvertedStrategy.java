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
import org.jetbrains.annotations.NotNull;

public class AssertBoolInvertedStrategy {
    private final static String messagePattern = "'%s(...)' should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 0 && (function.equals("assertTrue") || function.equals("assertFalse"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof UnaryExpression) {
                final UnaryExpression not = (UnaryExpression) param;
                if (null == not.getOperation() || PhpTokenTypes.opNOT != not.getOperation().getNode().getElementType()) {
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

    private static class TheLocalFix implements LocalQuickFix {
        final private String replacementFunction;
        final private SmartPsiElementPointer<PsiElement> argument;

        TheLocalFix(@NotNull String replacementFunction, @NotNull PsiElement argument) {
            super();
            final SmartPointerManager manager = SmartPointerManager.getInstance(argument.getProject());

            this.replacementFunction = replacementFunction;
            this.argument            = manager.createSmartPsiElementPointer(argument);
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
            if (expression instanceof FunctionReference && !project.isDisposed()) {
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
                call.handleElementRename(this.replacementFunction);
            }
        }
    }
}
