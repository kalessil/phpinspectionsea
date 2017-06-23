package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertBoolOfComparisonStrategy {
    private final static String messagePattern = "%m% should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (
            params.length > 0 && (
                function.equals("assertTrue")  || function.equals("assertNotTrue") ||
                function.equals("assertFalse") || function.equals("assertNotFalse")
            )
        ) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof BinaryExpression) {
                final BinaryExpression argument = (BinaryExpression) param;
                final PsiElement left           = argument.getLeftOperand();
                final PsiElement right          = argument.getRightOperand();
                if (null == left || null == right) {
                    return false;
                }

                final IElementType operation = argument.getOperationType();
                if (PhpTokenTypes.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                    final boolean isMethodInverting    = function.equals("assertFalse") || function.equals("assertNotTrue");
                    final boolean isOperationInverting = operation == PhpTokenTypes.opNOT_EQUAL || operation == PhpTokenTypes.opNOT_IDENTICAL;
                    final boolean isTypeStrict         = operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL;

                    final String replacementMethod = "assert" +
                        (isMethodInverting == isOperationInverting ? "" : "Not") + (isTypeStrict ? "Same" : "Equals");
                    final String message = messagePattern.replace("%m%", replacementMethod);

                    final TheLocalFix fixer = new TheLocalFix(replacementMethod, left, right);
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                    return true;
                }
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String replacementFunction;
        private PsiElement first;
        private PsiElement second;

        TheLocalFix(@NotNull String replacementFunction, @NotNull PsiElement first, @NotNull PsiElement second) {
            super();
            this.replacementFunction = replacementFunction;
            this.first               = first;
            this.second              = second;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
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

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(this.first);
                replaceParams[1].replace(this.second);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename(this.replacementFunction);
            }

            /* release a tree node reference */
            this.first  = null;
            this.second = null;
        }
    }

}
