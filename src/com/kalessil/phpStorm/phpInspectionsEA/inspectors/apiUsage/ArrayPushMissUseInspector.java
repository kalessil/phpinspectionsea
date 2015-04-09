package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayPushMissUseInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'%t%[] = ...' construction shall be used instead";
    private static final String strTargetFunctionName = "array_push";

    @NotNull
    public String getShortName() {
        return "ArrayPushMissUseInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check requirements */
                final PsiElement[] arrParams = reference.getParameters();
                final String strFunction     = reference.getName();
                if (2 != arrParams.length || StringUtil.isEmpty(strFunction) || !strFunction.equals(strTargetFunctionName)) {
                    return;
                }

                /* inspect given call */
                if (reference.getParent() instanceof StatementImpl) {
                    String strMessage = strProblemDescription.replace("%t%", arrParams[0].getText());
                    holder.registerProblem(reference.getFirstChild(), strMessage, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
