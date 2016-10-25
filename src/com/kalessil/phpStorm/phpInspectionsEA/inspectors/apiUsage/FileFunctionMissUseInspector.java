package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.ParameterListImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class FileFunctionMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'file_get_contents(%p%)' would consume less cpu and memory resources here";

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
                if (params.length != 1 || StringUtil.isEmpty(functionName) || !functionName.equals("file")) {
                    return;
                }

                /* function can be silenced, get parent for this case; validate parent structure */
                PsiElement parent = reference.getParent();
                if (parent instanceof UnaryExpressionImpl) {
                    final PsiElement operation = ((UnaryExpressionImpl) parent).getOperation();
                    if (null != operation && PhpTokenTypes.opSILENCE == operation.getNode().getElementType()) {
                        parent = parent.getParent();

                    }
                }
                if (!(parent instanceof ParameterListImpl) || !(parent.getParent() instanceof FunctionReferenceImpl)) {
                    return;
                }

                /* validate parent functions' name (implode or join) and amount of arguments */
                final FunctionReference parentReference = (FunctionReference) parent.getParent();
                final PsiElement[] parentParams         = parentReference.getParameters();
                final String parentFunctionName         = parentReference.getName();
                if (
                    parentParams.length != 2 || StringUtil.isEmpty(parentFunctionName) ||
                    (!parentFunctionName.equals("implode") && !parentFunctionName.equals("join"))) {
                    return;
                }

                /* validate if glue is not empty */
                final StringLiteralExpression glue = ExpressionSemanticUtil.resolveAsStringLiteral(parentParams[0]);
                if (null != glue && !StringUtil.isEmpty(glue.getContents())) {
                    return;
                }

                final String message = messagePattern.replace("%p%", params[0].getText());
                holder.registerProblem(parentReference, message, ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
