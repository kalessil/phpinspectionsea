package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Third parameter shall be provided to clarify if types safety important in this context";

    private HashSet<String> functionsSet = null;
    private HashSet<String> getFunctionsSet() {
        if (null == functionsSet) {
            functionsSet = new HashSet<>();

            functionsSet.add("array_search");
            functionsSet.add("in_array");
        }

        return functionsSet;
    }

    @NotNull
    public String getDisplayName() {
        return "Type compatibility: 'in_array(...)', 'array_search()' type unsafe usage";
    }

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                final String strFunction = reference.getName();
                if (intArgumentsCount != 2 || StringUtil.isEmpty(strFunction)) {
                    return;
                }

                if (getFunctionsSet().contains(strFunction)) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
