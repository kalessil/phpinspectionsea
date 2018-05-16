package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
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

public class PowerOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' can be used instead";

    @NotNull
    public String getShortName() {
        return "PowerOperatorCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP560) >= 0) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("pow")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2) {
                            final boolean wrapBase   = arguments[0] instanceof BinaryExpression ||
                                                       arguments[0] instanceof TernaryExpression;
                            final boolean wrapPower  = arguments[1] instanceof BinaryExpression ||
                                                       arguments[1] instanceof TernaryExpression;
                            final String replacement =
                                    (reference.getParent() instanceof BinaryExpression ? "(%b% ** %p%)" : "%b% ** %p%")
                                            .replace("%p%", wrapPower ? "(%p%)" : "%p%" )
                                            .replace("%b%", wrapBase ? "(%b%)" : "%b%")
                                            .replace("%p%", arguments[1].getText())
                                            .replace("%b%", arguments[0].getText());
                            holder.registerProblem(
                                    reference,
                                    String.format(messagePattern, replacement),
                                    new UseTheOperatorFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private class UseTheOperatorFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use ** operator instead";
        }

        UseTheOperatorFix(@NotNull String expression) {
            super(expression);
        }
    }
}
