package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

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
    private static final String strProblemDescription  = "This construct behaves as str_replace(%p%, ...), consider refactoring";

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
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("strtr")) {
                    return;
                }

                /* function usage with 3 parameters is the target pattern */
                PsiElement[] referenceParameters = reference.getParameters();
                if (3 == referenceParameters.length) {
                    /* ensure multiple search-replace are not packed into strings */
                    StringLiteralExpression search = ExpressionSemanticUtil.resolveAsStringLiteral(referenceParameters[1]);
                    if (null == search || StringUtil.isEmpty(search.getContents())) {
                        return;
                    }
                    String searchContent = search.getContents().replaceAll("\\\\(.)", "$1");
                    if (searchContent.length() > 1) {
                        return;
                    }

                    /* report case */
                    String strError = strProblemDescription
                            .replace("%p%", referenceParameters[1].getText()+", "+referenceParameters[2].getText());
                    holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}

