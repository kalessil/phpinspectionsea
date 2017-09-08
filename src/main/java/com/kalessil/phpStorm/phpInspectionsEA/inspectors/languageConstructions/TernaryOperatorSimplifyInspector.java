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
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String messagePattern = "'%r%' should be used instead";

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
                final PsiElement condition
                        = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    /* check branches; if both variants are identical, nested ternary inspection will spot it */
                    final PsiElement trueVariant
                            = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                    final PsiElement falseVariant
                            = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                    if (!PhpLanguageUtil.isBoolean(trueVariant) || !PhpLanguageUtil.isBoolean(falseVariant)) {
                        return;
                    }

                    final BinaryExpression binary = (BinaryExpression) condition;
                    final IElementType operator   = binary.getOperationType();
                    if (null == operator) {
                        return;
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
                                return;
                            }

                            replacement = "%l% %o% %r%"
                                    .replace("%r%", right.getText())
                                    .replace("%o%", oppositeOperators.get(operator))
                                    .replace("%l%", left.getText());
                        } else {
                            replacement = binary.getText();
                        }
                    }

                    final String message = messagePattern.replace("%r%", replacement);
                    holder.registerProblem(expression, message, new SimplifyFix(replacement));
                }
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