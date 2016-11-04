package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class ArraySearchUsedAsInArrayInspector extends BasePhpInspection {
    private static final String messageUseInArray        = "'in_array(...)' shall be used instead (clearer intention)";
    private static final String messageComparingWithTrue = "This makes no sense, as array_search(...) never returns true";

    @NotNull
    public String getShortName() {
        return "ArraySearchUsedAsInArrayInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (params.length < 2 || StringUtil.isEmpty(functionName) || !functionName.equals("array_search")) {
                    return;
                }

                /* check if the call used as (boolean) logical operand */
                if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    holder.registerProblem(reference, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                    return;
                }

                /* inspect implicit booleans comparison */
                if (reference.getParent() instanceof BinaryExpression) {
                    final BinaryExpression parent = (BinaryExpression) reference.getParent();
                    if (null != parent.getOperation() && null != parent.getOperation().getNode()) {
                        final IElementType operation = parent.getOperation().getNode().getElementType();
                        if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                            PsiElement secondOperand = parent.getLeftOperand();
                            if (secondOperand == reference) {
                                secondOperand = parent.getRightOperand();
                            }

                            if (
                                secondOperand instanceof ConstantReference &&
                                ExpressionSemanticUtil.isBoolean((ConstantReference) secondOperand)
                            ) {
                                final String booleanName = ((ConstantReference) secondOperand).getName();
                                /* should not compare true: makes no sense as it never returned */
                                if (!StringUtil.isEmpty(booleanName) && booleanName.equals("true")) {
                                    holder.registerProblem(secondOperand, messageComparingWithTrue, ProblemHighlightType.GENERIC_ERROR);
                                    return;
                                }

                                holder.registerProblem(parent, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                            }
                        }
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use in_array(...)";
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
                            UnaryExpressionImpl inverted = PhpPsiElementFactory.createFromText(project, UnaryExpressionImpl.class, "!$x");
                            //noinspection ConstantConditions - as we are safe from NPE here due to hardcoded pattern
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
