package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
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

public class NonSecureHtmlspecialcharsUsageInspector extends BasePhpInspection {
    private static final String message = "Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.";

    @NotNull
    public String getShortName() {
        return "NonSecureHtmlspecialcharsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("htmlspecialchars")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && !this.isTestContext(reference)) {
                        final String replacement = String.format("htmlspecialchars(%s, ENT_QUOTES)", arguments[0].getText());
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new EscapeAllQuotesFix(replacement));
                    }
                }
            }
        };
    }

    private static class EscapeAllQuotesFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Escape single and double quotes";
        }

        EscapeAllQuotesFix (@NotNull String expression) {
            super(expression);
        }
    }
}