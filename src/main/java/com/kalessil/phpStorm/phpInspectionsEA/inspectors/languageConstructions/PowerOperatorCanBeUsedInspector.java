package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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
    private static final String messagePattern = "'%e%' can be used instead";

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
                /* the operator was introduced in PHP 5.6 */
                final PhpLanguageLevel phpVersion
                        = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP560) >= 0) {
                    final PsiElement[] params = reference.getParameters();
                    final String functionName = reference.getName();
                    if (functionName != null && params.length == 2 && functionName.equals("pow")) {
                        final String replacement =
                                (reference.getParent() instanceof BinaryExpression ? "(%b% ** %p%)" : "%b% ** %p%")
                                        .replace("%p%", params[1] instanceof BinaryExpression ? "(%p%)" : "%p%" )
                                        .replace("%b%", params[0] instanceof BinaryExpression ? "(%b%)" : "%b%")
                                        .replace("%p%", params[1].getText())
                                        .replace("%b%", params[0].getText());
                        final String message = messagePattern.replace("%e%", replacement);
                        holder.registerProblem(reference, message, new UseTheOperatorFix(replacement));
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
