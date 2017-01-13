package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
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
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PsiElement trueVariant  = expression.getTrueVariant();
                final PsiElement falseVariant = expression.getFalseVariant();
                /* if both variants are identical, nested ternary inspection will spot it */
                if (!PhpLanguageUtil.isBoolean(trueVariant) || !PhpLanguageUtil.isBoolean(falseVariant)) {
                    return;
                }

                /* TODO: resolve type of other expressions */

                final PsiElement condition
                    = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary  = (BinaryExpression) condition;
                    final PsiElement operation     = binary.getOperation();
                    final IElementType operator    = null == operation ? null : operation.getNode().getElementType();
                    if (null == operator) {
                        return;
                    }

                    final String suggestedExpression;
                    final boolean useParenthesises = !oppositeOperators.containsKey(operator);
                    final boolean isInverted       = PhpLanguageUtil.isFalse(trueVariant);
                    if (useParenthesises) {
                        final boolean isLogical  = PhpTokenTypes.opAND == operator || PhpTokenTypes.opOR == operator;
                        final String boolCasting = isLogical ? "" : "(bool)";
                        suggestedExpression      = ((isInverted ? "!" : boolCasting) + "(%e%)").replace("%e%", binary.getText());
                    } else {
                        if (isInverted) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (null == left || null == right) {
                                return;
                            }

                            suggestedExpression = "%l% %o% %r%"
                                    .replace("%r%", right.getText())
                                    .replace("%o%", oppositeOperators.get(operator))
                                    .replace("%l%", left.getText());
                        } else {
                            suggestedExpression = binary.getText();
                        }
                    }

                    final String message = messagePattern.replace("%r%", suggestedExpression);
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new UseSuggestedReplacementFixer(suggestedExpression));
                }
            }
        };
    }
}