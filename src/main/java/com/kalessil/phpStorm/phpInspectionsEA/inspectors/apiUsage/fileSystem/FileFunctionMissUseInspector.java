package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
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
import org.apache.commons.lang.StringUtils;
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
    private static final String messagePattern = "'file_get_contents(%p%)' would consume less cpu and memory resources here.";

    @NotNull
    public String getShortName() {
        return "FileFunctionMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* validate parameters amount and function name (file) */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (params.length != 1 || StringUtils.isEmpty(functionName) || !functionName.equals("file")) {
                    return;
                }

                /* function can be silenced, get parent for this case; validate parent structure */
                PsiElement parent = reference.getParent();
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (null != operation && PhpTokenTypes.opSILENCE == operation.getNode().getElementType()) {
                        parent = parent.getParent();
                    }
                }
                if (!(parent instanceof ParameterList) || !OpenapiTypesUtil.isFunctionReference(parent.getParent())) {
                    return;
                }

                /* validate parent functions' name (implode or join) and amount of arguments */
                final FunctionReference parentReference = (FunctionReference) parent.getParent();
                final PsiElement[] parentParams         = parentReference.getParameters();
                final String parentFunctionName         = parentReference.getName();
                if (
                    parentParams.length != 2 || StringUtils.isEmpty(parentFunctionName) ||
                    (!parentFunctionName.equals("implode") && !parentFunctionName.equals("join"))) {
                    return;
                }

                /* validate if glue is not empty */
                final StringLiteralExpression glue = ExpressionSemanticUtil.resolveAsStringLiteral(parentParams[0]);
                if (null != glue && !StringUtils.isEmpty(glue.getContents())) {
                    return;
                }

                final String message = messagePattern.replace("%p%", params[0].getText());
                holder.registerProblem(parentReference, message, ProblemHighlightType.GENERIC_ERROR, new FileFunctionMissUseInspector.TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use file_get_contents(...)";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
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
                //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here and below
                fileGetContents.getParameters()[0].replace(fileFunctionReference.getParameters()[0].copy());

                //noinspection ConstantConditions
                expression.replace(replacement);
            }
        }
    }
}
