package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertBoolOfComparisonStrategy {
    private final static String messagePattern = "%m% would fit more here.";

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
                final IElementType operation    = argument.getOperationType();
                if (left != null && right != null && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                    final boolean isMethodInverting    = function.equals("assertFalse") || function.equals("assertNotTrue");
                    final boolean isOperationInverting = operation == PhpTokenTypes.opNOT_EQUAL || operation == PhpTokenTypes.opNOT_IDENTICAL;
                    final boolean isTypeStrict         = operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL;

                    final String replacementMethod = "assert" +
                        (isMethodInverting == isOperationInverting ? "" : "Not") + (isTypeStrict ? "Same" : "Equals");
                    final String message = messagePattern.replace("%m%", replacementMethod);
                    holder.registerProblem(reference, message, new TheLocalFix(replacementMethod, left, right));

                    return true;
                }
            }
        }

        return false;
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use suggested assertion instead";

        private final String replacementFunction;
        private final SmartPsiElementPointer<PsiElement> first;
        private final SmartPsiElementPointer<PsiElement> second;

        TheLocalFix(@NotNull String replacementFunction, @NotNull PsiElement first, @NotNull PsiElement second) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(first.getProject());

            this.replacementFunction = replacementFunction;
            this.first               = factory.createSmartPsiElementPointer(first);
            this.second              = factory.createSmartPsiElementPointer(second);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title + " (assert same or equal)";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement first      = this.first.getElement();
            final PsiElement second     = this.second.getElement();
            if (first != null && second != null && expression instanceof FunctionReference) {
                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 2 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(first);
                replaceParams[1].replace(second);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename(this.replacementFunction);
            }
        }
    }
}
