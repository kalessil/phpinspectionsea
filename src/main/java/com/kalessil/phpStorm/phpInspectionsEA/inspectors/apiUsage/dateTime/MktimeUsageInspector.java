package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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

public class MktimeUsageInspector extends BasePhpInspection {
    private static final String messageUseTime             = "You should use time() function instead (current usage produces a runtime warning).";
    private static final String messageParameterDeprecated = "Parameter 'is_dst' is deprecated and removed in PHP 7.";

    @NotNull
    public String getShortName() {
        return "MktimeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName       = reference.getName();
                final PsiElement[] params       = reference.getParameters();
                final boolean neededParamsCount = params.length == 0 || (params.length == 7 && !params[6].getText().isEmpty());
                if (neededParamsCount && functionName != null && (functionName.equals("mktime") || functionName.equals("gmmktime"))) {
                    if (params.length == 0) {
                        holder.registerProblem(reference, messageUseTime, ProblemHighlightType.WEAK_WARNING, new UseTimeFunctionLocalFix("time()"));
                    } else {
                        holder.registerProblem(params[6], messageParameterDeprecated, ProblemHighlightType.LIKE_DEPRECATED);
                    }
                }
            }
        };
    }

    private static class UseTimeFunctionLocalFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use time() instead";
        }

        UseTimeFunctionLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}