package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TernaryOperatorSimplifyInspector extends PhpInspection {
    private static final String messagePattern = "'%s' would make more sense here (simplification).";

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
    @Override
    public String getShortName() {
        return "TernaryOperatorSimplifyInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    /* case: binary condition */
                    final PsiElement trueVariant  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                    final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                    /* check branches; if both variants are identical, nested ternary inspection will spot it */
                    if (PhpLanguageUtil.isBoolean(trueVariant) && PhpLanguageUtil.isBoolean(falseVariant)) {
                        final String replacement = this.generateBinaryReplacement((BinaryExpression) condition, trueVariant);
                        if (replacement != null) {
                            final String message = String.format(messagePattern, replacement);
                            holder.registerProblem(expression, message, new SimplifyFix(replacement));
                        }
                    }
                } else {
                    /* condition might be inverted, extract it */
                    boolean isConditionInverted = false;
                    PsiElement candidate        = condition;
                    if (candidate instanceof UnaryExpression) {
                        final UnaryExpression unary = (UnaryExpression) candidate;
                        if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                            isConditionInverted = true;
                            candidate           = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                        }
                    }
                    /* case: emptiness check */
                    if (candidate instanceof PhpEmpty || candidate instanceof PhpIsset) {
                        final PsiElement trueVariant  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                        final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                        /* check branches; if both variants are identical, nested ternary inspection will spot it */
                        if (PhpLanguageUtil.isBoolean(trueVariant) && PhpLanguageUtil.isBoolean(falseVariant)) {
                            final boolean areBranchesInverted = PhpLanguageUtil.isFalse(trueVariant);
                            final boolean invert              = (isConditionInverted && !areBranchesInverted) ||
                                                                (areBranchesInverted && !isConditionInverted);
                            final String replacement          = (invert ? "!" : "") + candidate.getText();
                            holder.registerProblem(
                                    expression,
                                    String.format(messagePattern, replacement),
                                    new SimplifyFix(replacement)
                            );
                        }
                    }
                }
            }

            @Nullable
            private String generateBinaryReplacement(@NotNull BinaryExpression binary, @NotNull PsiElement trueVariant) {
                final IElementType operator = binary.getOperationType();
                if (null == operator) {
                    return null;
                }

                final String replacement;
                final boolean isInverted     = PhpLanguageUtil.isFalse(trueVariant);
                final boolean useParentheses = !oppositeOperators.containsKey(operator);
                if (useParentheses) {
                    final boolean isLogical  = PhpTokenTypes.opAND == operator || PhpTokenTypes.opOR == operator;
                    final String boolCasting = isLogical ? "" : "(bool)";
                    replacement              = ((isInverted ? "!" : boolCasting) + "(%e%)").replace("%e%", binary.getText());
                } else {
                    if (isInverted) {
                        final PsiElement left  = binary.getLeftOperand();
                        final PsiElement right = binary.getRightOperand();
                        if (left != null && right != null) {
                            replacement = String.format(
                                    "%s %s %s",
                                    left.getText(),
                                    oppositeOperators.get(operator),
                                    right.getText()
                            );
                        } else {
                            replacement = null;
                        }
                    } else {
                        replacement = binary.getText();
                    }
                }

                return replacement;
            }
        };
    }

    private static final class SimplifyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify the expression";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}