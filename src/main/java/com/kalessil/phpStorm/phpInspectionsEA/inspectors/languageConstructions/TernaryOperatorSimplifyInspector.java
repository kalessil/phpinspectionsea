package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be used instead.";

    private final static Map<IElementType, String> oppositeOperators = new HashMap<>();
    static {
        oppositeOperators.put(PhpTokenTypes.opEQUAL,            "!=");
        oppositeOperators.put(PhpTokenTypes.opIDENTICAL,        "!==");
        oppositeOperators.put(PhpTokenTypes.opNOT_EQUAL,        "==");
        oppositeOperators.put(PhpTokenTypes.opNOT_IDENTICAL,    "===");
        oppositeOperators.put(PhpTokenTypes.opGREATER,          "<=");
        oppositeOperators.put(PhpTokenTypes.opLESS,             ">=");
        oppositeOperators.put(PhpTokenTypes.opGREATER_OR_EQUAL, "<");
        oppositeOperators.put(PhpTokenTypes.opLESS_OR_EQUAL,    ">");
    }

    @NotNull
    public String getShortName() {
        return "TernaryOperatorSimplifyInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                final PsiElement rawCondition = expression.getCondition();
                final PsiElement condition    = ExpressionSemanticUtil.getExpressionTroughParenthesis(rawCondition);
                if (rawCondition != null && condition != null) {
                    final PsiElement trueVariant  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                    final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                    if (trueVariant != null && falseVariant != null) {
                        /* case: can be replaced with condition itself */
                        if (condition instanceof BinaryExpression) {
                            /* check branches; if both variants are identical, nested ternary inspection will spot it */
                            if (PhpLanguageUtil.isBoolean(trueVariant) && PhpLanguageUtil.isBoolean(falseVariant)) {
                                final String replacement = this.generateReplacement((BinaryExpression) condition, trueVariant);
                                if (replacement != null) {
                                    final String message = String.format(messagePattern, replacement);
                                    holder.registerProblem(expression, message, new SimplifyFix(replacement));
                                    return;
                                }
                            }
                        }

                        /* case: ternary can be relocated into the call from true+false variants */
                        if (trueVariant instanceof FunctionReference && falseVariant instanceof FunctionReference) {
                            final FunctionReference trueCall  = (FunctionReference) trueVariant;
                            final FunctionReference falseCall = (FunctionReference) falseVariant;
                            final String trueCallName         = trueCall.getName();
                            final String falseCallName        = falseCall.getName();
                            /* match calls names and arguments */
                            if (
                                trueCallName != null && trueCallName.equals(falseCallName) &&
                                trueCall.getClass() == falseVariant.getClass()
                            ) {
                                final PsiElement[] trueArguments  = trueCall.getParameters();
                                final PsiElement[] falseArguments = falseCall.getParameters();
                                if (trueArguments.length > 0 && trueArguments.length == falseArguments.length) {
                                    int mismatchedCount          = 0;
                                    final List<String> fragments = new ArrayList<>(trueArguments.length);
                                    for (int index = 0; index < trueArguments.length; ++index) {
                                        if (!PsiEquivalenceUtil.areElementsEquivalent(trueArguments[index], falseArguments[index])) {
                                            if (++mismatchedCount > 1) {
                                                break;
                                            }
                                            fragments.add(String.format(
                                                "%s ? %s : %s",
                                                rawCondition.getText(),
                                                trueArguments[index].getText(),
                                                falseArguments[index].getText()
                                            ));
                                        } else {
                                            fragments.add(trueArguments[index].getText());
                                        }
                                    }
                                    if (mismatchedCount == 1) {
                                        String prefix = "";
                                        if (trueCall instanceof MethodReference) {
                                            final MethodReference reference = (MethodReference) trueCall;
                                            final PsiElement operator       = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                                            if (operator != null) {
                                                prefix = reference.getFirstChild().getText() + operator.getText();
                                            }
                                        }
                                        final String replacement = prefix + String.format("%s(%s)", trueCallName, String.join(", ", fragments));
                                        final String message     = String.format(messagePattern, replacement);
                                        holder.registerProblem(expression, message, new SimplifyFix(replacement));
                                        // return;
                                    }
                                    fragments.clear();
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private String generateReplacement(@NotNull BinaryExpression binary, @NotNull PsiElement trueVariant) {
                final IElementType operator = binary.getOperationType();
                if (null == operator) {
                    return null;
                }

                final String replacement;
                final boolean useParenthesises = !oppositeOperators.containsKey(operator);
                final boolean isInverted       = PhpLanguageUtil.isFalse(trueVariant);
                if (useParenthesises) {
                    final boolean isLogical  = PhpTokenTypes.opAND == operator || PhpTokenTypes.opOR == operator;
                    final String boolCasting = isLogical ? "" : "(bool)";
                    replacement              = ((isInverted ? "!" : boolCasting) + "(%e%)").replace("%e%", binary.getText());
                } else {
                    if (isInverted) {
                        final PsiElement left  = binary.getLeftOperand();
                        final PsiElement right = binary.getRightOperand();
                        if (null == left || null == right) {
                            return null;
                        }

                        replacement = "%l% %o% %r%"
                                .replace("%r%", right.getText())
                                .replace("%o%", oppositeOperators.get(operator))
                                .replace("%l%", left.getText());
                    } else {
                        replacement = binary.getText();
                    }
                }

                return replacement;
            }
        };
    }

    private class SimplifyFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Simplify the expression";
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}