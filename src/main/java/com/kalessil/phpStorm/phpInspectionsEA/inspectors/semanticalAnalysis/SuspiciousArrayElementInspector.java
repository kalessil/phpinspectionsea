package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class SuspiciousArrayElementInspector extends BasePhpInspection {
    private static final String messagePattern = "There is chance that it should be %s here.";

    @NotNull
    public String getShortName() {
        return "SuspiciousArrayElementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                for (final ArrayHashElement element : expression.getHashElements()) {
                    final PsiElement key = element.getKey();
                    if (key instanceof Variable) {
                        final PsiElement value = element.getValue();
                        if (value != null && OpenapiEquivalenceUtil.areEqual(key, value)) {
                            final String replacement = String.format("'%s'", ((Variable) key).getName());
                            holder.registerProblem(
                                    key,
                                    String.format(messagePattern, replacement),
                                    new UseStringKeyFix(replacement)
                            );
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
            return title;
        }

        UseStringKeyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
