package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
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

public class StringsFirstCharactersCompareInspector extends BasePhpInspection {
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
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (
                    functionName != null && arguments.length == 3 &&
                    (functionName.equals("strncmp") || functionName.equals("strncasecmp")) &&
                    arguments[2].getNode().getElementType() == PhpElementTypes.NUMBER
                ) {
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
                        try {
                            final String string = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                            isTarget            = Integer.valueOf(arguments[2].getText()) != string.length();
                        } catch (NumberFormatException lengthParsingHasFailed) {
                            isTarget = false;
                        }
                        if (isTarget) {
                            holder.registerProblem(arguments[2], "The specified length doesn't match the string length.");
                        }
                    }
                }
            }
        };
    }
}
