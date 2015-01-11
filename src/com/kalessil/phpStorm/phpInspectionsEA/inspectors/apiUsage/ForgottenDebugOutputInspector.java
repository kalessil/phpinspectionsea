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

public class ForgottenDebugOutputInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Please ensure this is not forgotten debug statement";

    private HashSet<String> functionsSet = null;
    private HashSet<String> getFunctionsSet() {
        if (null == functionsSet) {
            functionsSet = new HashSet<>();

            functionsSet.add("print_r");
            functionsSet.add("var_export");
            functionsSet.add("var_dump" );
        }

        return functionsSet;
    }

    @NotNull
    public String getDisplayName() {
        return "Probable bugs: forgotten debug statements";
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                if (intArgumentsCount != 1) {
                    return;
                }

                final String strFunction = reference.getName();
                if (StringUtil.isEmpty(strFunction) || !getFunctionsSet().contains(strFunction)) {
                    return;
                }

                holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
