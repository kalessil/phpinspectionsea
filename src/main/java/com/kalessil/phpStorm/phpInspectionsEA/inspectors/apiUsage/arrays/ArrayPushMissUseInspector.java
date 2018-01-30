package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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

public class ArrayPushMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be used instead (2x faster).";

    @NotNull
    public String getShortName() {
        return "ArrayPushMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_push")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && OpenapiTypesUtil.isStatementImpl(reference.getParent())) {
                        /* inspect given call: single instruction, 2nd parameter is not variadic */
                        PsiElement variadicCandidate = arguments[1].getPrevSibling();
                        if (variadicCandidate instanceof PsiWhiteSpace) {
                            variadicCandidate = variadicCandidate.getPrevSibling();
                        }
                        if (!OpenapiTypesUtil.is(variadicCandidate, PhpTokenTypes.opVARIADIC)) {
                            final String replacement = String.format("%s[] = %s", arguments[0].getText(), arguments[1].getText());
                            holder.registerProblem(
                                    reference,
                                    String.format(messagePattern, replacement),
                                    new UseElementPushFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private class UseElementPushFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use []= instead";
        }

        UseElementPushFix(@NotNull String expression) {
            super(expression);
        }
    }
}
