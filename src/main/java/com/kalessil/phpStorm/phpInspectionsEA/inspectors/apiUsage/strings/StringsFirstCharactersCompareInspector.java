package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
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

public class StringsFirstCharactersCompareInspector extends BasePhpInspection {
    private static final String message = "The specified length doesn't match the string length.";

    @NotNull
    public String getShortName() {
        return "StringsFirstCharactersCompareInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("strncmp") || functionName.equals("strncasecmp"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 3 && OpenapiTypesUtil.isNumber(arguments[2])) {
                        /* find out if we have a string literal in arguments */
                        final StringLiteralExpression literal;
                        if (arguments[1] instanceof StringLiteralExpression) {
                            literal = (StringLiteralExpression) arguments[1];
                        } else if (arguments[0] instanceof StringLiteralExpression) {
                            literal = (StringLiteralExpression) arguments[0];
                        } else {
                            literal = null;
                        }
                        /* if so, do deeper inspection */
                        if (literal != null) {
                            boolean isTarget;
                            int stringLength;
                            try {
                                final String string = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                                stringLength        = string.length();
                                isTarget            = stringLength != Integer.parseInt(arguments[2].getText());
                            } catch (NumberFormatException lengthParsingHasFailed) {
                                isTarget     = false;
                                stringLength = -1;
                            }
                            if (isTarget && stringLength != -1) {
                                holder.registerProblem(arguments[2], message, new LengthFix(String.valueOf(stringLength)));
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class LengthFix extends UseSuggestedReplacementFixer {
        private static final String title = "Set correct value for the length parameter";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        LengthFix(@NotNull String expression) {
            super(expression);
        }
    }
}
