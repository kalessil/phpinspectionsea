package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class LowPerformanceArrayUniqueUsageInspector extends BasePhpInspection {
    private static final String strProblemUseArrayKeysWithCountValues = "array_keys(array_count_values(<expression>)) will be more performing (but provide comments)";
    private static final String strProblemUseCountWithCountValues     = "count(array_count_values(<expression>)) will be more performing (but provide comments)";

    @NotNull
    public String getShortName() {
        return "LowPerformanceArrayUniqueUsageInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /** try filtering by args count first */
                PsiElement[] parameters = reference.getParameters();
                final int intParamsCount = parameters.length;
                if (intParamsCount != 1) {
                    return;
                }
                /** now naming filter */
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("array_unique")) {
                    return;
                }

                /** check it's nested call */
                if (reference.getParent() instanceof ParameterList) {
                    ParameterList params = (ParameterList) reference.getParent();
                    if (params.getParent() instanceof FunctionReference) {
                        FunctionReference parentCall = (FunctionReference) params.getParent();
                        /** look up for parent function name */
                        String strParentFunction = parentCall.getName();
                        if (! StringUtil.isEmpty(strParentFunction)) {

                            /** === test array_values(array_unique(<expression>)) case === */
                            if (strParentFunction.equals("array_values")) {
                                holder.registerProblem(parentCall, strProblemUseArrayKeysWithCountValues, ProblemHighlightType.WEAK_WARNING);
                                return;
                            }

                            /** === test count(array_unique(<expression>)) case === */
                            if (strParentFunction.equals("count")) {
                                holder.registerProblem(parentCall, strProblemUseCountWithCountValues, ProblemHighlightType.WEAK_WARNING);
                                // return;
                            }
                        }
                    }
                }
            }

        };
    }
}
