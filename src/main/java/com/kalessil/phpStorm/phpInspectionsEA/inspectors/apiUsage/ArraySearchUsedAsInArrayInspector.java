package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
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
                    holder.registerProblem(reference, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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

                                holder.registerProblem(parent, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }
            }
        };
    }
}
