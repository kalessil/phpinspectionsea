package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
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

public class StrtotimeUsageInspector extends BasePhpInspection {
    private static final String messageUseTime  = "'time()' should be used instead (2x faster).";
    private static final String messageDropTime = "'time()' is default valued already, it can safely be removed.";

    @NotNull
    public String getShortName() {
        return "StrtotimeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("strtotime")) {
                    return;
                }
                final PsiElement[] params = reference.getParameters();
                if (params.length == 0 || params.length > 2) {
                    return;
                }

                /* handle case: strtotime("now") -> time() */
                if (params.length == 1) {
                    if (params[0] instanceof StringLiteralExpression) {
                        final StringLiteralExpression pattern = (StringLiteralExpression) params[0];
                        if (pattern.getContents().equalsIgnoreCase("now")) {
                            holder.registerProblem(reference, messageUseTime, new UseTimeFunctionLocalFix("time()"));
                        }
                    }
                }
                /* handle case: strtotime(..., time()) -> date(...) */
                else if (params.length == 2) {
                    if (OpenapiTypesUtil.isFunctionReference(params[1])) {
                        final String callName = ((FunctionReference) params[1]).getName();
                        if (callName != null && callName.equals("time")) {
                            final String replacement = "strtotime(%a%)".replace("%a%", params[0].getText());
                            holder.registerProblem(
                                    reference,
                                    messageDropTime,
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                    new DropTimeFunctionCallLocalFix(replacement)
                            );
                        }
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

    private static class DropTimeFunctionCallLocalFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Drop unnecessary time() call";
        }

        DropTimeFunctionCallLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
