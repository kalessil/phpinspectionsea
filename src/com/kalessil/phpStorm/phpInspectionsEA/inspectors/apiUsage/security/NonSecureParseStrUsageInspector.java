package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class NonSecureParseStrUsageInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "Please provide second parameter to not influence globals";

    @NotNull
    public String getShortName() {
        return "NonSecureParseStrUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunction = reference.getName();
                if (
                    1 == reference.getParameters().length &&
                    !StringUtil.isEmpty(strFunction) &&
                    (strFunction.equals("parse_str") || strFunction.equals("mb_parse_str"))
                ) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}
