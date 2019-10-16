package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

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
    private static final String messagePattern = "'%s' would make more sense here (reduces cyclomatic and cognitive complexity).";

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
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition != null && this.isTargetCondition(condition)) {
                    final PsiElement firstValue  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                    if (firstValue != null) {
                        final PsiElement secondValue = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                        if (secondValue != null) {
                            final boolean isDirect  = PhpLanguageUtil.isTrue(firstValue) && PhpLanguageUtil.isFalse(secondValue);
                            final boolean isReverse = !isDirect && PhpLanguageUtil.isTrue(secondValue) && PhpLanguageUtil.isFalse(firstValue);
                            if (isDirect || isReverse) {
                                boolean isInverted = condition instanceof UnaryExpression;
                                if (isInverted) {
                                    condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(((UnaryExpression) condition).getValue());
                                }
                                if (condition != null) {
                                    final boolean invert     = (isDirect && isInverted) || (isReverse && !isInverted);
                                    final String replacement = this.generateReplacement(condition, invert);
                                    holder.registerProblem(
                                            expression,
                                            String.format(ReportingUtil.wrapReportedMessage(messagePattern), replacement),
                                            new SimplifyFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }

            @NotNull
            private String generateReplacement(@NotNull PsiElement condition, boolean invert) {
                String replacement = (invert ? "!" : "") + condition.getText();

                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    final IElementType operator   = binary.getOperationType();
                    if (operator != null) {
                        if (!oppositeOperators.containsKey(operator)) {
                            final String casting = (PhpTokenTypes.opAND == operator || PhpTokenTypes.opOR == operator) ? "" : "(bool)";
                            replacement          = ((invert ? "!" : casting) + "(%e%)").replace("%e%", binary.getText());
                        } else if (invert) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (left != null && right != null) {
                                replacement = String.format("%s %s %s", left.getText(), oppositeOperators.get(operator), right.getText());
                            }
                        }
                    }
                }

                return replacement;
            }

            private boolean isTargetCondition(@NotNull PsiElement condition) {
                if (condition instanceof BinaryExpression || condition instanceof PhpIsset || condition instanceof PhpEmpty) {
                    return true;
                } else if (condition instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) condition;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                        final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                        if (argument != null) {
                            return this.isTargetCondition(argument);
                        }
                    }
                } else if (condition instanceof FunctionReference) {
                    return this.isTargetFunction((FunctionReference) condition);
                }
                return false;
            }

            private boolean isTargetFunction(@NotNull FunctionReference reference) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof Function) {
                    final Function function = (Function) resolved;
                    boolean isTarget        = OpenapiElementsUtil.getReturnType(function) != null;
                    if (!isTarget && function.getName().startsWith("is_")) {
                        final String location = function.getContainingFile().getVirtualFile().getCanonicalPath();
                        isTarget              = location != null && location.contains(".jar!") && location.contains("/stubs/");
                    }
                    return isTarget && function.getType().equals(PhpType.BOOLEAN);
                }
                return false;
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