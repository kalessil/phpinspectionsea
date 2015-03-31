package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ClassInStringContextStrategy;
import org.jetbrains.annotations.NotNull;

public class StrlenInEmptyStringCheckContextInspection extends BasePhpInspection {
    private static final String strProblemDescription                      = "Can be replaced by comparing with empty string";
    private static final String strProblemDescriptionObjectUsed            = "Can be replaced with ''' == $...' construction";
    private static final String strProblemDescriptionMissingToStringMethod = "%class% miss __toString() implementation";

    @NotNull
    public String getShortName() {
        return "StrlenInEmptyStringCheckContextInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("strlen")) {
                    return;
                }

                boolean isMatchedPattern = false;
                PsiElement warningTarget = null;

                /* check explicit numbers comparisons */
                if (reference.getParent() instanceof BinaryExpression) {
                    BinaryExpression objParent = (BinaryExpression) reference.getParent();
                    PsiElement objOperation    = objParent.getOperation();
                    if (null != objOperation && null != objOperation.getNode()) {
                        /* collect second operand */
                        PsiElement secondOperand = objParent.getLeftOperand();
                        if (secondOperand == reference) {
                            secondOperand = objParent.getRightOperand();
                        }
                        /* second operand shall be a number */
                        if (secondOperand instanceof  PhpExpression && PhpElementTypes.NUMBER == secondOperand.getNode().getElementType()) {
                            String strNumber = secondOperand.getText();

                            /* check cases when comparing with 1 */
                            IElementType operationType = objOperation.getNode().getElementType();
                            if (operationType == PhpTokenTypes.opLESS || operationType == PhpTokenTypes.opGREATER_OR_EQUAL) {
                                /* comparison with 1 supported currently in NON-yoda style TODO: yoda style support */
                                isMatchedPattern = strNumber.equals("1") && objParent.getLeftOperand() == reference;
                                warningTarget    = objParent;
                            }

                            /* check cases when comparing with 0 */
                            if (!isMatchedPattern && (
                                operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL ||
                                operationType == PhpTokenTypes.opEQUAL || operationType == PhpTokenTypes.opNOT_EQUAL
                            )) {
                                isMatchedPattern = strNumber.equals("0");
                                warningTarget    = objParent;
                            }
                        }
                    }
                }

                /* checks NON-implicit boolean comparison patternS */
                if (!isMatchedPattern && ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    isMatchedPattern = true;
                    warningTarget    = reference;
                }

                /* investigate possible issues */
                if (isMatchedPattern) {
                    final int argumentsCount       = reference.getParameters().length;

                    /* first evaluate if any object casting issues presented */
                    if (
                        argumentsCount > 0 &&
                        ClassInStringContextStrategy.apply(reference.getParameters()[0], holder, warningTarget, strProblemDescriptionMissingToStringMethod)
                    ) {
                        holder.registerProblem(reference.getParent(), strProblemDescriptionObjectUsed, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }

                    /* report issues */
                    holder.registerProblem(warningTarget, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}