package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
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

public class FilePutContentsMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' would consume less cpu and memory resources here.";

    @NotNull
    public String getShortName() {
        return "FilePutContentsMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("file_put_contents")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2) {
                        /* inner call can be silenced, un-wrap it */
                        PsiElement innerCandidate = arguments[1];
                        if (innerCandidate instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) innerCandidate;
                            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opSILENCE)) {
                                innerCandidate = unary.getValue();
                            }
                        }
                        /* analyze the call */
                        if (OpenapiTypesUtil.isFunctionReference(innerCandidate)) {
                            final FunctionReference innerReference = (FunctionReference) innerCandidate;
                            final String innerName                 = innerReference.getName();
                            if (innerName != null && innerName.equals("file_get_contents")) {
                                final PsiElement[] innerArguments = innerReference.getParameters();
                                if (innerArguments.length == 1) {
                                    final String replacement = "copy(%s%, %d%)"
                                            .replace("%s%", innerArguments[0].getText())
                                            .replace("%d%", arguments[0].getText());
                                    holder.registerProblem(
                                            reference,
                                            String.format(messagePattern, replacement),
                                            ProblemHighlightType.GENERIC_ERROR,
                                            new UseCopyFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseCopyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use copy(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseCopyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
