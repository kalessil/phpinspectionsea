package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.parser.PhpElementTypes;
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

public class SubStrUsedAsArrayAccessInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' might be used instead (invalid index accesses might show up).";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length < 3 || null == functionName || !functionName.equals("substr")) {
                    return;
                }

                /* false-positive: PHP 5.3 is not supporting `call()[index]` constructs */
                if (params[0] instanceof FunctionReference) {
                    final PhpLanguageLevel php
                            = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                    if (php == PhpLanguageLevel.PHP530) {
                        return;
                    }
                }

                if (PhpElementTypes.NUMBER == params[2].getNode().getElementType() && params[2].getText().equals("1")) {
                    final boolean isNegativeOffset = params[1].getText().startsWith("-");
                    final String expression        = (isNegativeOffset ? "%c%[strlen(%c%) %i%]" : "%c%[%i%]")
                        .replace("%c%", params[0].getText())
                        .replace("%c%", params[0].getText())
                        .replace("%i%", params[1].getText());

                    final String message = messagePattern.replace("%e%", expression);
                    holder.registerProblem(reference, message, new TheLocalFix(expression));
                }
            }
        };
    }

    private static class TheLocalFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use array access";
        }

        public TheLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
