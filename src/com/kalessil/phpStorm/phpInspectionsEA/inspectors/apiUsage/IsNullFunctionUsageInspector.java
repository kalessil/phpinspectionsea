package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionIsNull  = "'null === ...' construction shall be used instead";
    private static final String strProblemDescriptionNotNull = "'null !== ...' construction shall be used instead";
    private static final String strIsNull = "is_null";

    @NotNull
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check parameters amount and name */
                final String strFunctionName = reference.getName();
                final int parametersCount    = reference.getParameters().length;
                if (1 != parametersCount || StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals(strIsNull)) {
                    return;
                }

                /* decide which message to use */
                String strError = strProblemDescriptionIsNull;
                if (reference.getParent() instanceof UnaryExpression) {
                    PsiElement objOperation = ((UnaryExpression) reference.getParent()).getOperation();
                    if (null != objOperation && PhpTokenTypes.opNOT == objOperation.getNode().getElementType()) {
                        strError = strProblemDescriptionNotNull;
                    }
                }

                /* report the issue */
                holder.registerProblem(reference, strError, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}