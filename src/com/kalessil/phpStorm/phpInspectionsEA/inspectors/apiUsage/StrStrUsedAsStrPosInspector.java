package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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
                /* check if it's the target function */
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("strstr")) {
                    return;
                }

                /* checks implicit boolean comparison pattern */
                if (reference.getParent() instanceof BinaryExpression) {
                    BinaryExpression objParent = (BinaryExpression) reference.getParent();
                    PsiElement objOperation    = objParent.getOperation();
                    if (null != objOperation && null != objOperation.getNode()) {
                        IElementType operationType = objOperation.getNode().getElementType();
                        if (operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL) {
                            /* get second operand */
                            PsiElement objSecondOperand = objParent.getLeftOperand();
                            if (objSecondOperand == reference) {
                                objSecondOperand = objParent.getRightOperand();
                            }

                            /* verify if operand is a boolean and report an issue */
                            if (objSecondOperand instanceof ConstantReference && ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondOperand)) {
                                holder.registerProblem(objParent, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                return;
                            }
                        }
                    }
                }

                /* checks NON-implicit boolean comparison patternS */
                if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
            }
        };
    }
}
