package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryBooleanCheckInspector extends PhpInspection {
    private static final String message = "'%s' would fit better here (reduces cognitive load).";

    @NotNull
    public String getShortName() {
        return "UnnecessaryBooleanCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final IElementType operation = expression.getOperationType();
                if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                    final List<PsiElement> booleans = Stream.of(expression.getLeftOperand(), expression.getRightOperand())
                            .filter(PhpLanguageUtil::isBoolean)
                            .collect(Collectors.toList());
                    if (booleans.size() == 1 && ExpressionSemanticUtil.getBlockScope(expression) != null) {
                        final PsiElement bool   = booleans.get(0);
                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(expression, bool);
                        if (second != null) {
                            /* extract value */
                            boolean isValueInverted = false;
                            PsiElement value        = ExpressionSemanticUtil.getExpressionTroughParenthesis(second);
                            if (second instanceof UnaryExpression) {
                                final UnaryExpression unary = (UnaryExpression) second;
                                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                    value           = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                                    isValueInverted = true;
                                }
                            }
                            if (value != null && this.isBooleanTypeOnly(value)) {
                                final boolean invertValue = (isValueInverted && operation == PhpTokenTypes.opIDENTICAL && PhpLanguageUtil.isTrue(bool)) ||
                                                            (!isValueInverted && operation == PhpTokenTypes.opNOT_IDENTICAL && PhpLanguageUtil.isTrue(bool)) ||
                                                            (isValueInverted && operation == PhpTokenTypes.opNOT_IDENTICAL && PhpLanguageUtil.isFalse(bool)) ||
                                                            (!isValueInverted && operation == PhpTokenTypes.opIDENTICAL && PhpLanguageUtil.isFalse(bool));
                                final String replacement  = (invertValue ? "!" : "") + value.getText();
                                holder.registerProblem(expression, String.format(message, replacement), new SimplifyFix(replacement));
                            }
                        }
                    }
                    booleans.clear();
                }
            }

            private boolean isBooleanTypeOnly(@NotNull PsiElement expression) {
                if (expression instanceof PhpTypedElement) {
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, holder.getProject());
                    if (resolved != null) {
                        return resolved.size() == 1 &&
                               !resolved.hasUnknown() &&
                               Types.getType(resolved.getTypes().iterator().next()).equals(Types.strBoolean);
                    }
                }
                return false;
            }
        };
    }

    private static final class SimplifyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify expression";

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
