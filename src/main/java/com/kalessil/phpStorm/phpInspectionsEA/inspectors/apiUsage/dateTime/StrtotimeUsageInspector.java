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
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == 0 || arguments.length > 2) {
                    return;
                }

                /* handle case: strtotime("now") -> time() */
                if (arguments.length == 1) {
                    if (arguments[0] instanceof StringLiteralExpression) {
                        final StringLiteralExpression pattern = (StringLiteralExpression) arguments[0];
                        if (pattern.getContents().equalsIgnoreCase("now")) {
                            holder.registerProblem(reference, messageUseTime, new UseTimeFunctionLocalFix());
                        }
                    }
                }
                /* handle case: strtotime(..., time()) -> date(...) */
                else if (arguments.length == 2) {
                    if (OpenapiTypesUtil.isFunctionReference(arguments[1])) {
                        final String callName = ((FunctionReference) arguments[1]).getName();
                        if (callName != null && callName.equals("time")) {
                            final String replacement = "strtotime(%a%)".replace("%a%", arguments[0].getText());
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

    private static final class UseTimeFunctionLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use time() instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseTimeFunctionLocalFix() {
            super("time()");
        }
    }

    private static final class DropTimeFunctionCallLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Drop unnecessary time() call";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        DropTimeFunctionCallLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
