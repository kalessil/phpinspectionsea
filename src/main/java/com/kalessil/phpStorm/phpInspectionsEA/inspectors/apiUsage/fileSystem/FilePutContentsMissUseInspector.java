package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
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

public class FilePutContentsMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' would consume less cpu and memory resources here";

    @NotNull
    public String getShortName() {
        return "FilePutContentsMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* validate parameters amount and function name (file) */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (2 != params.length || StringUtil.isEmpty(functionName) || !functionName.equals("file_put_contents")) {
                    return;
                }

                /* inner call can be silenced, un-wrap it */
                PsiElement innerCandidate = params[1];
                if (innerCandidate instanceof UnaryExpression) {
                    final PsiElement operator = ((UnaryExpression) innerCandidate).getOperation();
                    if (null != operator && PhpTokenTypes.opSILENCE == operator.getNode().getElementType()) {
                        innerCandidate = ((UnaryExpression) innerCandidate).getValue();
                    }
                }

                /* analyze the call */
                if (innerCandidate instanceof FunctionReference) {
                    final FunctionReference innerReference = (FunctionReference) innerCandidate;
                    final String innerName                 = innerReference.getName();
                    final PsiElement[] innerParams         = innerReference.getParameters();
                    /* check if matches the target pattern */
                    if (1 == innerParams.length && !StringUtil.isEmpty(innerName) && innerName.equals("file_get_contents")) {
                        final String pattern = "copy(%s%, %d%)"
                                .replace("%s%", innerParams[0].getText())
                                .replace("%d%", params[0].getText());
                        final String message = messagePattern.replace("%e%", pattern);
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix(pattern));
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        public TheLocalFix(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                ParenthesizedExpression replacement = PhpPsiElementFactory.createFromText(project, ParenthesizedExpression.class, "(" + this.expression + ")");
                if (null != replacement) {
                    expression.replace(replacement.getArgument());
                }
            }
        }
    }
}
