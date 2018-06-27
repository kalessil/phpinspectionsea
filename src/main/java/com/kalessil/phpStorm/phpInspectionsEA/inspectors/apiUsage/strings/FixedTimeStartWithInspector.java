package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FixedTimeStartWithInspector extends BasePhpInspection {

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("stripos", "strncasecmp");
        mapping.put("strpos",  "strncmp");
    }

    private static final String messagePattern  = "'%s' would be a solution not depending on the string length.";

    @NotNull
    public String getShortName() {
        return "FixedTimeStartWithInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && mapping.containsKey(functionName)) {
                    final PsiElement parent = reference.getParent();
                    if (parent instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) parent;
                        final IElementType operator   = binary.getOperationType();
                        if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 2 && arguments[1] instanceof StringLiteralExpression) {
                                final PsiElement zeroCandidate = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                if (zeroCandidate != null && zeroCandidate.getText().equals("0")) {
                                    final StringLiteralExpression literal = (StringLiteralExpression) arguments[1];
                                    if (literal.getFirstPsiChild() == null) {
                                        final String replacement = String.format(
                                                "%s(%s, %s, %s)",
                                                mapping.get(functionName),
                                                arguments[0].getText(),
                                                arguments[1].getText(),
                                                PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote()).length()
                                        );
                                        holder.registerProblem(
                                                reference,
                                                String.format(messagePattern, replacement),
                                                new UseFirstCharactersCompareFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseFirstCharactersCompareFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use fixed-time operation instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseFirstCharactersCompareFix(@NotNull String expression) {
            super(expression);
        }
    }
}
