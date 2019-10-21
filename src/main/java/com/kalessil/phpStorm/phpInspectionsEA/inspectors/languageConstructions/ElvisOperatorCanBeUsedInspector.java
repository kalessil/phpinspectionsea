package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ElvisOperatorCanBeUsedInspector extends PhpInspection {
    private static final String message = "' ... ?: ...' construction should be used instead.";

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
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }

                if (!expression.isShort()) {
                    final PsiElement trueRaw       = expression.getTrueVariant();
                    final PsiElement trueExtracted = ExpressionSemanticUtil.getExpressionTroughParenthesis(trueRaw);
                    if (trueRaw != null && trueExtracted != null) {
                        final PsiElement conditionExtracted = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                        if (conditionExtracted != null) {
                            if (conditionExtracted != trueExtracted && OpenapiEquivalenceUtil.areEqual(conditionExtracted, trueExtracted)) {
                                holder.registerProblem(
                                        trueRaw,
                                        ReportingUtil.wrapReportedMessage(message),
                                        new TheLocalFix()
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use ?: instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                /* cleanup spaces around */
                PsiElement before = target.getPrevSibling();
                if (before instanceof PsiWhiteSpace) {
                    before.delete();
                }
                PsiElement after = target.getNextSibling();
                if (after instanceof PsiWhiteSpace) {
                    after.delete();
                }
                /* drop true expression */
                target.delete();
            }
        }
    }
}
