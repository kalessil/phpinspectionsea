package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
                if (functionName == null || !functionName.equals("substr")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length < 3) {
                    return;
                }

                /* false-positive: PHP 5.3 is not supporting `call()[index]` constructs */
                if (arguments[0] instanceof FunctionReference) {
                    final PhpLanguageLevel php
                            = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                    if (php == PhpLanguageLevel.PHP530) {
                        return;
                    }
                }

                if (OpenapiTypesUtil.isNumber(arguments[2]) && arguments[2].getText().equals("1")) {
                    final boolean isNegativeOffset = arguments[1].getText().startsWith("-");
                    final String expression        = (isNegativeOffset ? "%c%[strlen(%c%) %i%]" : "%c%[%i%]")
                        .replace("%c%", arguments[0].getText())
                        .replace("%c%", arguments[0].getText())
                        .replace("%i%", arguments[1].getText());

                    final String message = messagePattern.replace("%e%", expression);
                    holder.registerProblem(reference, message, new TheLocalFix(expression));
                }
            }
        };
    }

    private static final class TheLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use array access";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        TheLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
