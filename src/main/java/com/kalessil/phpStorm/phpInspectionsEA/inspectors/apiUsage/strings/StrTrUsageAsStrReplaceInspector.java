package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StrTrUsageAsStrReplaceInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' can be used instead (improves maintainability).";

    @NotNull
    public String getShortName() {
        return "StrTrUsageAsStrReplaceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("strtr")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 3) {
                        /* ensure multiple search-replace are not packed into strings */
                        final StringLiteralExpression search = ExpressionSemanticUtil.resolveAsStringLiteral(arguments[1]);
                        if (search != null) {
                            final String content = search.getContents();
                            if (!content.isEmpty() && content.length() < 3) {
                                final boolean isTarget;
                                if (search.isSingleQuote()) {
                                    /* original regex: ^(.|\\[\\'])$ */
                                    isTarget = content.matches("^(.|\\\\[\\\\'])$");
                                } else {
                                    /* original regex: ^(.|\\[\\"$rnt])$*/
                                    isTarget = content.matches("^(.|\\\\[\\\\\"$rnt])$");
                                }
                                if (isTarget) {
                                    final String replacement = String.format("str_replace(%s, %s, %s)",
                                            arguments[1].getText(),
                                            arguments[2].getText(),
                                            arguments[0].getText()
                                    );
                                    holder.registerProblem(
                                            reference,
                                            String.format(messagePattern, replacement),
                                            new UseStringReplaceFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseStringReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use str_replace(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStringReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}

