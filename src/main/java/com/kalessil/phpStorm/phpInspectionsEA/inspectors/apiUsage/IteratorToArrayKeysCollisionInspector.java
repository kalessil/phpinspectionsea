package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IteratorToArrayKeysCollisionInspector extends BasePhpInspection {
    private static final String messagePattern = "Second parameter should be provided to clarify keys collisions handling.";

    @NotNull
    public String getShortName() {
        return "IteratorToArrayKeysCollisionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("iterator_to_array")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final String replacement = String.format(
                                "%s%s(%s, false)",
                                reference.getImmediateNamespaceName(),
                                functionName,
                                arguments[0].getText()
                        );
                        holder.registerProblem(
                                reference,
                                messagePattern,
                                new IgnoreOriginalKeysFix(replacement)
                        );
                    }
                }
            }
        };
    }
    private static final class IgnoreOriginalKeysFix extends UseSuggestedReplacementFixer {
        private static final String title = "Add 'false' as the second argument";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        IgnoreOriginalKeysFix(@NotNull String expression) {
            super(expression);
        }
    }
}