package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
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
    private static final String messagePattern = "'file_get_contents(%s)' would consume less cpu and memory resources here.";

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
                                        holder.registerProblem(
                                                outerCall,
                                                String.format(messagePattern, arguments[0].getText()),
                                                new TheLocalFix()
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

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use file_get_contents(...)";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, FunctionReference.class, "file_get_contents($x)");
                final FunctionReference fileGetContents = (FunctionReference) replacement;

                PsiElement fileFunction = ((FunctionReference) expression).getParameters()[1];
                if (fileFunction instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) fileFunction).getOperation();
                    if (null != operation && PhpTokenTypes.opSILENCE == operation.getNode().getElementType()) {
                        fileFunction = ((UnaryExpression) fileFunction).getValue();
                    }
                }

                final FunctionReference fileFunctionReference = (FunctionReference) fileFunction;
                fileGetContents.getParameters()[0].replace(fileFunctionReference.getParameters()[0].copy());
                expression.replace(replacement);
            }
        }
    }
}
