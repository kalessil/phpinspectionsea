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
    private static final String messagePattern = "'%e%' can be used instead (improves maintainability).";

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
                        if (search != null && !search.getContents().isEmpty()) {
                            final String searchContent = search.getContents().replaceAll("\\\\(.)", "$1");
                            if (searchContent.length() == 1) {
                                final String replacement = "str_replace(%s%, %r%, %t%)"
                                        .replace("%t%", arguments[0].getText())
                                        .replace("%r%", arguments[2].getText())
                                        .replace("%s%", arguments[1].getText());
                                final String message = messagePattern.replace("%e%", replacement);
                                holder.registerProblem(reference, message, new UseStringReplaceFix(replacement));
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

