package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class MktimeUsageInspection extends BasePhpInspection {
    private static final String strProblemUseTime  = "Please use the time() function instead (produces runtime warning)";
    private static final String strProblemParameterDeprecated = "Parameter 'is_dst' is deprecated and removed in v7";

    @NotNull
    public String getShortName() {
        return "MktimeUsageInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check parameters amount and name */
                final String strFunctionName = reference.getName();
                final int parametersCount    = reference.getParameters().length;
                if (
                    StringUtil.isEmpty(strFunctionName) ||
                    !((0 == parametersCount ||  7 == parametersCount) && strFunctionName.equals("mktime"))
                ) {
                    return;
                }

                /* report the issue */
                if (0 == parametersCount) {
                    holder.registerProblem(reference, strProblemUseTime, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                } else {
                    holder.registerProblem(reference.getParameters()[6], strProblemParameterDeprecated, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}