package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ClassInStringContextStrategy;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StrlenInEmptyStringCheckContextInspection extends BasePhpInspection {
    private static final String messagePattern               = "'%e%' can be used instead.";
    private static final String patternMissingToStringMethod = "%class% miss __toString() implementation.";

    @NotNull
    public String getShortName() {
        return "StrlenInEmptyStringCheckContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                if (functionName == null || (!functionName.equals("strlen") && !functionName.equals("mb_strlen"))) {
                    return;
                }

                boolean isMatchedPattern = false;
                boolean isEmptyString    = false;
                PsiElement warningTarget = null;

                /* check explicit numbers comparisons */
                if (reference.getParent() instanceof BinaryExpression) {
                    final BinaryExpression parent  = (BinaryExpression) reference.getParent();
                    final PsiElement left          = parent.getLeftOperand();
                    final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(parent, reference);
                    /* second operand should be a number */
                    if (OpenapiTypesUtil.isNumber(secondOperand)) {
                        final String strNumber = secondOperand.getText();

                        /* check cases when comparing with 1 */
                        final IElementType operationType = parent.getOperationType();
                        if (operationType == PhpTokenTypes.opLESS || operationType == PhpTokenTypes.opGREATER_OR_EQUAL) {
                            /* comparison with 1 supported currently in NON-yoda style TODO: yoda style support */
                            isMatchedPattern = left == reference && strNumber.equals("1");
                            warningTarget    = parent;
                            isEmptyString    = operationType == PhpTokenTypes.opLESS;
                        }

                        /* check cases when comparing with 0 */
                        if (!isMatchedPattern && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operationType)) {
                            isMatchedPattern = strNumber.equals("0");
                            warningTarget    = parent;
                            isEmptyString    = operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opEQUAL;
                        }
                    }
                }

                /* checks NON-implicit boolean comparison patterns */
                if (!isMatchedPattern && ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    isMatchedPattern = true;
                    warningTarget    = reference;

                    final PsiElement parent = reference.getParent();
                    final PsiElement operation
                            = parent instanceof UnaryExpression ? ((UnaryExpression) parent).getOperation() : null;
                    if (operation != null) {
                        isEmptyString = operation.getNode().getElementType() == PhpTokenTypes.opNOT;
                        warningTarget = parent;
                    }
                }

                /* investigate possible issues */
                final PsiElement[] params = reference.getParameters();
                if (isMatchedPattern && params.length > 0) {
                    final boolean isStrict
                        = !ClassInStringContextStrategy.apply(params[0], holder, warningTarget, patternMissingToStringMethod);

                    final String replacement = "'' %o% %a%"
                            .replace("%a%", params[0].getText())
                            .replace("%o%", (isEmptyString ? "=" : "!") + (isStrict ? "==" : "="));
                    final String message = messagePattern.replace("%e%", replacement);
                    holder.registerProblem(warningTarget, message, ProblemHighlightType.WEAK_WARNING, new CompareToEmptyStringFix(replacement));
                }
            }
        };
    }

    private class CompareToEmptyStringFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use empty string comparison instead";
        }

        CompareToEmptyStringFix(@NotNull String expression) {
            super(expression);
        }
    }
}