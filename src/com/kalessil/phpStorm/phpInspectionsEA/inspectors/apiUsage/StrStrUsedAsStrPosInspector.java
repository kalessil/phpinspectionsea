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

public class StrStrUsedAsStrPosInspector extends BasePhpInspection {
    private static final String strProblemDescription  = "'false <op> strpos(...)' shall be used instead";

    @NotNull
    public String getShortName() {
        return "StrStrUsedAsStrPosInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName)) {
                    return;
                }

                if (reference.getParent() instanceof BinaryExpression) {
                    BinaryExpression objParent = (BinaryExpression) reference.getParent();
                    if (null != objParent.getOperation() && null != objParent.getOperation().getNode()) {
                        IElementType objOperation = objParent.getOperation().getNode().getElementType();
                        /** === use-case implicit boolean test === */
                        if (
                            (objOperation == PhpTokenTypes.opIDENTICAL || objOperation == PhpTokenTypes.opNOT_IDENTICAL) &&
                            strFunctionName.equals("strstr")
                        ) {
                            PsiElement objSecondOperand = objParent.getLeftOperand();
                            if (objSecondOperand == reference) {
                                objSecondOperand = objParent.getRightOperand();
                            }

                            if (
                                objSecondOperand instanceof ConstantReference &&
                                ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondOperand)
                            ) {
                                holder.registerProblem(objParent, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                return;
                            }
                        }

                        /** === use-case complex NON implicit boolean test === */
                        if (
                            (objOperation == PhpTokenTypes.opAND || objOperation == PhpTokenTypes.opOR) &&
                            strFunctionName.equals("strstr")
                        ) {
                            holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }

                /** === use-case single implicit boolean test with ! === */
                if (reference.getParent() instanceof UnaryExpression) {
                    PsiElement objOperation = ((UnaryExpression) reference.getParent()).getOperation();
                    if (null != objOperation) {
                        IElementType typeOperation = objOperation.getNode().getElementType();
                        if (typeOperation == PhpTokenTypes.opNOT && strFunctionName.equals("strstr")) {
                            holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }

                /** === use-case single NON implicit boolean test === */
                if (reference.getParent() instanceof If && strFunctionName.equals("strstr")) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
            }
        };
    }
}
