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
                final PsiElement[] params    = reference.getParameters();
                final String strFunctionName = reference.getName();
                if (params.length < 2 || StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("array_search")) {
                    return;
                }

                if (reference.getParent() instanceof BinaryExpression) {
                    final BinaryExpression objParent = (BinaryExpression) reference.getParent();
                    if (null != objParent.getOperation() && null != objParent.getOperation().getNode()) {
                        final IElementType objOperation = objParent.getOperation().getNode().getElementType();
                        /* === use-case implicit boolean test === */
                        if (objOperation == PhpTokenTypes.opIDENTICAL || objOperation == PhpTokenTypes.opNOT_IDENTICAL) {
                            PsiElement objSecondOperand = objParent.getLeftOperand();
                            if (objSecondOperand == reference) {
                                objSecondOperand = objParent.getRightOperand();
                            }

                            if (
                                objSecondOperand instanceof ConstantReference &&
                                ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondOperand)
                            ) {
                                final String booleanName = ((ConstantReference) objSecondOperand).getName();
                                if (!StringUtil.isEmpty(booleanName) && booleanName.equals("true")) {
                                    holder.registerProblem(objSecondOperand, messageComparingWithTrue, ProblemHighlightType.GENERIC_ERROR);
                                    return;
                                }

                                holder.registerProblem(objParent, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                return;
                            }
                        }

                        /* === use-case complex NON implicit boolean test === */ /* TODO: ExpressionSemanticUtil.isUsedAsLogicalOperand */
                        if (
                            objOperation == PhpTokenTypes.opAND || objOperation == PhpTokenTypes.opLIT_AND ||
                            objOperation == PhpTokenTypes.opOR  || objOperation == PhpTokenTypes.opLIT_OR
                        ) {
                            holder.registerProblem(reference, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }

                /* === use-case single implicit boolean test with ! === */ /* TODO: ExpressionSemanticUtil.isUsedAsLogicalOperand */
                if (reference.getParent() instanceof UnaryExpression) {
                    final PsiElement objOperation = ((UnaryExpression) reference.getParent()).getOperation();
                    if (null != objOperation) {
                        final IElementType typeOperation = objOperation.getNode().getElementType();
                        if (typeOperation == PhpTokenTypes.opNOT) {
                            holder.registerProblem(reference, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }

                /* === use-case single NON implicit boolean test === */ /* TODO: ExpressionSemanticUtil.isUsedAsLogicalOperand */
                if (reference.getParent() instanceof If) {
                    holder.registerProblem(reference, messageUseInArray, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    // return;
                }
            }
        };
    }
}
