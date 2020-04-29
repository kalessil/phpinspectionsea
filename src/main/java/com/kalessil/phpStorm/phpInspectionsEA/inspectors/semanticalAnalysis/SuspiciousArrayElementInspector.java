package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class SuspiciousArrayElementInspector extends PhpInspection {
    private static final String messagePattern = "There is chance that it should be %s here.";

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousArrayElementInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious array element";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                for (final ArrayHashElement element : expression.getHashElements()) {
                    final PsiElement key = element.getKey();
                    if (key instanceof Variable) {
                        final PsiElement value = element.getValue();
                        if (value != null && OpenapiEquivalenceUtil.areEqual(key, value)) {
                            final String replacement = String.format("'%s'", ((Variable) key).getName());
                            holder.registerProblem(
                                    key,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    new UseStringKeyFix(replacement)
                            );
                        }
                    } else if (key instanceof StringLiteralExpression) {
                        final StringLiteralExpression literal = (StringLiteralExpression) key;
                        if (literal.getFirstPsiChild() == null) {
                            final String content = literal.getContents();
                            if (! content.isEmpty() && ! content.trim().equals(content)) {
                                final String replacement = String.format("'%s'", content.trim());
                                holder.registerProblem(
                                        key,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                        new UseStringKeyFix(replacement)
                                );
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final ArrayIndex index = expression.getIndex();
                if (index != null) {
                    final PsiElement key = index.getValue();
                    if (key instanceof StringLiteralExpression) {
                        final StringLiteralExpression literal = (StringLiteralExpression) key;
                        if (literal.getFirstPsiChild() == null) {
                            final String content = literal.getContents();
                            if (! content.isEmpty() && ! content.trim().equals(content)) {
                                final String replacement = String.format("'%s'", content.trim());
                                holder.registerProblem(
                                        key,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                        new UseStringKeyFix(replacement)
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseStringKeyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Replace with string key";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseStringKeyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
