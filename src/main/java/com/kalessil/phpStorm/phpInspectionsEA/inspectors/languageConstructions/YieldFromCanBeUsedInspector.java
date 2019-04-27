package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpYield;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class YieldFromCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'yield from ...' can be used instead (generator delegation).";

    // Inspection options.
    public boolean ONLY_KEY_VALUE_YIELDS = false;

    @NotNull
    public String getShortName() {
        return "YieldFromCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP700) < 0)           { return; }

                final PsiElement source = statement.getArray();
                if (source != null) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(statement);
                    if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                        final PsiElement instruction = ExpressionSemanticUtil.getLastStatement(body);
                        if (instruction != null) {
                            final PsiElement yieldCandidate = instruction.getFirstChild();
                            if (yieldCandidate instanceof PhpYield) {
                                final PsiElement[] yieldChildren = yieldCandidate.getChildren();
                                if (yieldChildren.length > 0 && !yieldCandidate.getText().startsWith("yield from ")) {
                                    final PsiElement yieldValue = yieldChildren.length == 2 ? yieldChildren[1] : yieldChildren[0];
                                    final PsiElement value      = statement.getValue();
                                    if (yieldValue != null && value != null && OpenapiEquivalenceUtil.areEqual(yieldValue, value)) {
                                        final PsiElement yieldKey = yieldChildren.length == 2 ? yieldChildren[0] : null;
                                        final PsiElement key      = statement.getKey();
                                        final boolean isTarget =
                                                (yieldKey == key && key == null && !ONLY_KEY_VALUE_YIELDS) ||
                                                (yieldKey != null && key != null && OpenapiEquivalenceUtil.areEqual(yieldKey, key));
                                        if (isTarget) {
                                            final String replacement = String.format("yield from %s", source.getText());
                                            holder.registerProblem(
                                                    statement.getFirstChild(),
                                                    message,
                                                    new UseYieldFromFix(replacement)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Only for yielded key-value pairs", ONLY_KEY_VALUE_YIELDS, (value) -> ONLY_KEY_VALUE_YIELDS = value)
        );
    }

    private static final class UseYieldFromFix implements LocalQuickFix {
        private static final String title = "Use 'yield from ...' instead";

        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        UseYieldFromFix(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement target = expression.getParent();
                if (target != null) {
                    target.replace(PhpPsiElementFactory.createStatement(project, this.expression + ';'));
                }
            }
        }
    }
}