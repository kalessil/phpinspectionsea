package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Variable;
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
    private static final String messagePattern = "'%s' might be used instead (invalid index accesses might show up).";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("substr")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 3) {
                        final PsiElement length = arguments[2];
                        if (OpenapiTypesUtil.isNumber(length) && length.getText().equals("1")) {
                            final PsiElement source = arguments[0];
                            final boolean isTarget  = source instanceof Variable ||
                                                      source instanceof ArrayAccessExpression ||
                                                      source instanceof FieldReference;
                            if (isTarget) {
                                final PsiElement offset        = arguments[1];
                                final boolean isNegativeOffset = offset.getText().startsWith("-");
                                final String expression        = (isNegativeOffset ? "%c%[strlen(%c%) %i%]" : "%c%[%i%]")
                                        .replace("%c%", source.getText())
                                        .replace("%c%", source.getText())
                                        .replace("%i%", isNegativeOffset ? offset.getText().replaceFirst("-", "- "): offset.getText());

                                holder.registerProblem(
                                        reference,
                                        String.format(messagePattern, expression),
                                        new TheLocalFix(expression)
                                );
                            }
                        }
                    }
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
