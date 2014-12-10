package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String strProblemDescription = "By default this call is type un-safe. Provide third " +
            "parameter to make it type sensitive.";
    private final String strTargetFunctions = "array_search,in_array";

    @NotNull
    public String getDisplayName() {
        return "API: type un-safe array search";
    }

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                if (intArgumentsCount != 2) {
                    return;
                }

                final String strFunction = reference.getName();
                if (null == strFunction || strFunction.equals("")) {
                    return;
                }

                final boolean isTargetFunction = strTargetFunctions.contains(strFunction.toLowerCase());
                if (!isTargetFunction) {
                    return;
                }

                holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
