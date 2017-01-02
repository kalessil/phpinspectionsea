package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class StrTrUsageAsStrReplaceInspector extends BasePhpInspection {
    private static final String messagePattern = "This construct behaves as str_replace(%p%, ...), consider refactoring (improves maintainability).";

    @NotNull
    public String getShortName() {
        return "StrTrUsageAsStrReplaceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (3 != params.length || StringUtil.isEmpty(functionName) || !functionName.equals("strtr")) {
                    return;
                }

                /* ensure multiple search-replace are not packed into strings */
                final StringLiteralExpression search = ExpressionSemanticUtil.resolveAsStringLiteral(params[1]);
                if (null == search || StringUtil.isEmpty(search.getContents())) {
                    return;
                }
                final String searchContent = search.getContents().replaceAll("\\\\(.)", "$1");
                if (searchContent.length() > 1) {
                    return;
                }

                /* report the case */
                final String message = messagePattern.replace("%p%", params[1].getText()+", "+params[2].getText());
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}

