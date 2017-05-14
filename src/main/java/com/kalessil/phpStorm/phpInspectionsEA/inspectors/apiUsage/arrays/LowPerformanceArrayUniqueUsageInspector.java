package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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

public class LowPerformanceArrayUniqueUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' would be more efficient (make sure to leave a comment to explain the intent).";

    @NotNull
    public String getShortName() {
        return "LowPerformanceArrayUniqueUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* try filtering by args count first */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (params.length != 1 || functionName == null || !functionName.equals("array_unique")) {
                    return;
                }

                /* check the context */
                final PsiElement context = reference.getParent().getParent();
                if (OpenapiTypesUtil.isFunctionReference(context)) {
                    final FunctionReference parentCall = (FunctionReference) context;
                    final String parentFunctionName    = parentCall.getName();
                    if (parentFunctionName != null) {
                        /* test array_values(array_unique(<expression>)) case */
                        if (parentFunctionName.equals("array_values")) {
                            final String replacement = "array_keys(array_count_values(%a%))".replace("%a%", params[0].getText());
                            final String message     = messagePattern.replace("%e%", replacement);
                            holder.registerProblem(parentCall, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new ReplaceFix(replacement));
                            return;
                        }

                        /* test count(array_unique(<expression>)) case */
                        if (parentFunctionName.equals("count")) {
                            final String replacement = "count(array_count_values(%a%))".replace("%a%", params[0].getText());
                            final String message     = messagePattern.replace("%e%", replacement);
                            holder.registerProblem(parentCall, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new ReplaceFix(replacement));
                            // return;
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
            return "Optimize array_unique(...) usage";
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
