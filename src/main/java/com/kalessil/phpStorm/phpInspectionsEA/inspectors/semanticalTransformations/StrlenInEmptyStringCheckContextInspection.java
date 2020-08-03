package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
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
    private static final String messagePattern = "'%s' would make more sense here (it also slightly faster).";

    @NotNull
    @Override
    public String getShortName() {
        return "StrlenInEmptyStringCheckContextInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'strlen(...)' misused";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("strlen") || functionName.equals("mb_strlen"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0 && ExpressionSemanticUtil.getBlockScope(reference) != null) {
                        boolean isMatchedPattern = false;
                        boolean isEmptyString    = false;
                        PsiElement target        = null;

                        /* check explicit numbers comparisons */
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary  = (BinaryExpression) parent;
                            final PsiElement left          = binary.getLeftOperand();
                            final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(binary, reference);
                            /* second operand should be a number */
                            if (OpenapiTypesUtil.isNumber(secondOperand)) {
                                final String number = secondOperand.getText();

                                /* check cases when comparing with 1 */
                                final IElementType operator = binary.getOperationType();
                                if (operator == PhpTokenTypes.opGREATER) {
                                    isMatchedPattern = left == reference && number.equals("0");
                                    target           = binary;
                                    isEmptyString    = false;
                                } else if (operator == PhpTokenTypes.opLESS || operator == PhpTokenTypes.opGREATER_OR_EQUAL) {
                                    isMatchedPattern = left == reference && number.equals("1");
                                    target           = binary;
                                    isEmptyString    = operator == PhpTokenTypes.opLESS;
                                }

                                /* check cases when checking equality to 0 */
                                if (!isMatchedPattern && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                                    isMatchedPattern = number.equals("0");
                                    target           = binary;
                                    isEmptyString    = operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opEQUAL;
                                }
                            }
                        }

                        /* checks NON-implicit boolean comparison patterns */
                        if (!isMatchedPattern && ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                            isMatchedPattern           = true;
                            target                     = reference;
                            final PsiElement operation = parent instanceof UnaryExpression ? ((UnaryExpression) parent).getOperation() : null;
                            if (operation != null) {
                                isEmptyString = OpenapiTypesUtil.is(operation, PhpTokenTypes.opNOT);
                                target        = parent;
                            }
                        }

                        /* investigate possible issues */
                        if (isMatchedPattern) {
                            final boolean isRegular  = ComparisonStyle.isRegular();
                            final String operator    = (isEmptyString ? "=" : "!") + (this.canApplyIdentityOperator(arguments[0]) ? "==" : "=");
                            final String replacement = String.format(
                                    isRegular ? "%s %s ''" : "'' %s %s",
                                    isRegular ? arguments[0].getText() : operator,
                                    isRegular ? operator : arguments[0].getText()
                            );
                            holder.registerProblem(
                                    target,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    new CompareToEmptyStringFix(replacement)
                            );
                        }
                    }
                }
            }

            private boolean canApplyIdentityOperator(@NotNull PsiElement value) {
                if (value instanceof PhpTypedElement) {
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) value, holder.getProject());
                    if (resolved != null && resolved.size() == 1) {
                        return Types.strString.equals(Types.getType(resolved.getTypes().iterator().next()));
                    }
                }
                return false;
            }
        };
    }

    private static final class CompareToEmptyStringFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use empty string comparison instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        CompareToEmptyStringFix(@NotNull String expression) {
            super(expression);
        }
    }
}