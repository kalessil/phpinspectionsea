package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ElvisOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "It's possible to use '%s' here (shorter notation).";

    @NotNull
    @Override
    public String getShortName() {
        return "ElvisOperatorCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Elvis operator can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression ternary) {
                if (! ternary.isShort()) {
                    final PsiElement condition   = ExpressionSemanticUtil.getExpressionTroughParenthesis(ternary.getCondition());
                    final PsiElement trueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(ternary.getTrueVariant());
                    if (condition != null && trueVariant != null) {
                        final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(ternary.getFalseVariant());
                        if (falseVariant != null && OpenapiEquivalenceUtil.areEqual(condition, trueVariant)) {
                            final String replacement = String.format(
                                    "%s ?: %s",
                                    ternary.getCondition().getText(),
                                    ternary.getFalseVariant().getText()
                            );
                            holder.registerProblem(
                                    ternary,
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, replacement)),
                                    new UseElvisOperatorFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseElvisOperatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use '... ?: ...' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseElvisOperatorFix(@NotNull String expression) {
            super(expression);
        }
    }
}
