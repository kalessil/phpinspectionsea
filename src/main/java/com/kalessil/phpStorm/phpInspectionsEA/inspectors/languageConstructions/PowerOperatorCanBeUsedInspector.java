package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
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
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* the operator was introduced in PHP 5.6 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP560) < 0) {
                    return;
                }

                /* verify the call signature */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (null == functionName || 2 != params.length || !functionName.equals("pow")) {
                    return;
                }

                /* report and suggest QF-ing */
                final String expression = "%b% ** %p%".replace("%p%", params[1].getText()).replace("%b%", params[0].getText());
                final String message = messagePattern.replace("%e%", expression);
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new UseSuggestedReplacementFixer(expression));
            }
        };
    }
}
