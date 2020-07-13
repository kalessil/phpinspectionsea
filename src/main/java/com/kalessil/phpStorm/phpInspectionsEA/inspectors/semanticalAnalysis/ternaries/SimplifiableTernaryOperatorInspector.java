package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ternaries;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SimplifiableTernaryOperatorInspector extends PhpInspection {
    private static final String messagePattern = "'%s ? ... : ...' can be used instead (reduces cognitive load, improves maintainability)";

    @NotNull
    @Override
    public String getShortName() {
        return "SimplifiableTernaryOperatorInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Simplifiable ternary operator";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement positive  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                final PsiElement negative  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                final PsiElement condition = expression.getCondition();
                if (condition != null && negative != null && positive instanceof TernaryExpression) {
                    final TernaryExpression nestedTernary = (TernaryExpression) positive;
                    final PsiElement nestedCondition      = nestedTernary.getCondition();
                    final PsiElement nestedPositive       = ExpressionSemanticUtil.getExpressionTroughParenthesis(nestedTernary.getTrueVariant());
                    final PsiElement nestedNegative       = ExpressionSemanticUtil.getExpressionTroughParenthesis(nestedTernary.getFalseVariant());
                    if (nestedCondition != null && nestedPositive != null && nestedNegative != null && ! nestedTernary.isShort()) {
                        final boolean isTarget = OpenapiEquivalenceUtil.areEqual(negative, nestedNegative);
                        if (isTarget) {
                            final String newCondition = String.format("(%s && %s)", condition.getText(), nestedCondition.getText());
                            final String replacement  = String.format("%s ? %s : %s", newCondition, nestedPositive.getText(), nestedNegative.getText());
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), newCondition),
                                    new MergeTernariesFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class MergeTernariesFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify ternary";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        MergeTernariesFix(@NotNull String expression) {
            super(expression);
        }
    }
}
