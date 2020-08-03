package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArraySearchUsedAsInArrayInspector extends BasePhpInspection {
    private static final String messageUseInArray        = "'in_array(...)' would fit more here (clarifies intention, improves maintainability).";
    private static final String messageComparingWithTrue = "This makes no sense, as array_search(...) never returns true.";

    @NotNull
    @Override
    public String getShortName() {
        return "ArraySearchUsedAsInArrayInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'array_search(...)' could be replaced by 'in_array(...)'";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_search")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                            /* case: used as (boolean) logical operand */
                            holder.registerProblem(
                                    reference,
                                    MessagesPresentationUtil.prefixWithEa(messageUseInArray),
                                    new TheLocalFix()
                            );
                        } else {
                            /* case: implicit booleans comparison */
                            final PsiElement parent = reference.getParent();
                            if (parent instanceof BinaryExpression) {
                                final BinaryExpression binary = (BinaryExpression) parent;
                                final IElementType operation  = binary.getOperationType();
                                if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                                    final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                    if (PhpLanguageUtil.isBoolean(secondOperand)) {
                                        if (PhpLanguageUtil.isTrue(secondOperand)) {
                                            holder.registerProblem(
                                                    secondOperand,
                                                    MessagesPresentationUtil.prefixWithEa(messageComparingWithTrue),
                                                    ProblemHighlightType.GENERIC_ERROR
                                            );
                                        } else {
                                            holder.registerProblem(
                                                    binary,
                                                    MessagesPresentationUtil.prefixWithEa(messageUseInArray),
                                                    new TheLocalFix()
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use in_array(...)";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                if (expression instanceof FunctionReference) {
                    ((FunctionReference) expression).handleElementRename("in_array");
                }
                if (expression instanceof BinaryExpression) {
                    final BinaryExpression comparingFalse = (BinaryExpression) expression;
                    final PsiElement operation            = comparingFalse.getOperation();
                    if (null != operation) {
                        PsiElement call = comparingFalse.getLeftOperand();
                        if (call instanceof ConstantReference) {
                            call = comparingFalse.getRightOperand();
                        }
                        if (call instanceof FunctionReference) {
                            /* rename the call */
                            ((FunctionReference) call).handleElementRename("in_array");
                            if (PhpTokenTypes.opIDENTICAL == operation.getNode().getElementType()) {
                                /* we want false, hence need invert the call */
                                UnaryExpression inverted = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, "!$x");
                                inverted.getValue().replace(call);
                                expression.replace(inverted);
                            } else {
                                expression.replace(call);
                            }
                        }
                    }
                }
            }
        }
    }
}
