package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

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

public class ArrayUniqueCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' would be more readable here (array_unique(...) was optimized in PHP 7.2-beta3+).";

    /* UTing purposes, as language level 7.2 is not available in older environments */
    public boolean FORCE_ANALYSIS = false;

    @NotNull
    public String getShortName() {
        return "ArrayUniqueCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                final String functionName    = reference.getName();
                if (arguments.length != 1 || functionName == null || !functionName.equals("array_count_values")) {
                    return;
                }

                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (FORCE_ANALYSIS || phpVersion.compareTo(PhpLanguageLevel.PHP710) > 0) {
                    final PsiElement context = reference.getParent().getParent();
                    if (OpenapiTypesUtil.isFunctionReference(context)) {
                        final String parentFunctionName = ((FunctionReference) context).getName();
                        if (parentFunctionName != null) {
                            if (parentFunctionName.equals("array_keys")) {
                                final String replacement = "array_values(array_unique(%a%))".replace("%a%", arguments[0].getText());
                                final String message     = messagePattern.replace("%e%", replacement);
                                holder.registerProblem(context, message, new ReplaceFix(replacement));
                            } else if (parentFunctionName.equals("count")) {
                                final String replacement = "count(array_unique(%a%))".replace("%a%", arguments[0].getText());
                                final String message     = messagePattern.replace("%e%", replacement);
                                holder.registerProblem(context, message, new ReplaceFix(replacement));
                            }
                        }
                    }
                }
            }
        };
    }

    private class ReplaceFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use array_unique(...) instead";
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
