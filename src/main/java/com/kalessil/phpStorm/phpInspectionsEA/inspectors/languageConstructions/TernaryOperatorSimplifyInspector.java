package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be used instead (reduces cyclomatic and cognitive complexity).";

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

    @NotNull
    @Override
    public String getDisplayName() {
        return "Ternary operator could be simplified";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                final PsiElement rawCondition = expression.getCondition();
                if (rawCondition != null) {
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(rawCondition);
                    if (condition instanceof BinaryExpression) {
                        final PsiElement trueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                        if (trueVariant != null) {
                            final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                            if (falseVariant != null) {
                                final boolean isTarget = PhpLanguageUtil.isBoolean(trueVariant) && PhpLanguageUtil.isBoolean(falseVariant);
                                if (isTarget) {
                                    final String replacement = this.generateReplacement((BinaryExpression) condition, trueVariant);
                                    if (replacement != null) {
                                        holder.registerProblem(
                                                expression,
                                                MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, replacement)),
                                                new SimplifyFix(replacement)
                                        );
                                    }
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
                final boolean useParentheses = !oppositeOperators.containsKey(operator);
                final boolean isInverted     = PhpLanguageUtil.isFalse(trueVariant);
                if (useParentheses) {
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

    private static final class SimplifyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify the expression";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}