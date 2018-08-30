package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class FileFunctionMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' would consume less cpu and memory resources here.";

    @NotNull
    public String getShortName() {
        return "FileFunctionMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("file")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        /* function can be silenced, get parent for this case */
                        PsiElement parent = reference.getParent();
                        if (parent instanceof UnaryExpression) {
                            final PsiElement operation = ((UnaryExpression) parent).getOperation();
                            if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opSILENCE)) {
                                parent = parent.getParent();
                            }
                        }
                        if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                final FunctionReference outerCall = (FunctionReference) grandParent;
                                final String outerName            = outerCall.getName();
                                if (outerName != null && outerName.equals("implode")) {
                                    final PsiElement[] outerArguments = outerCall.getParameters();
                                    boolean isTarget                  = outerArguments.length == 1;
                                    if (!isTarget && outerArguments.length == 2) {
                                        final StringLiteralExpression literal = ExpressionSemanticUtil.resolveAsStringLiteral(outerArguments[0]);
                                        isTarget  = literal != null && literal.getContents().isEmpty();
                                    }
                                    if (isTarget) {
                                        final String replacement = String.format("file_get_contents(%s)", arguments[0].getText());
                                        holder.registerProblem(
                                                outerCall,
                                                String.format(messagePattern, replacement),
                                                new UseFileGetContentsFix(replacement)
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

    private static final class UseFileGetContentsFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use file_get_contents(...)";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseFileGetContentsFix(@NotNull String expression) {
            super(expression);
        }
    }
}
