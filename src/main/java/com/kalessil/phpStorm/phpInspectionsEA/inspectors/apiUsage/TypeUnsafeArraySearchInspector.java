package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String strProblemDescription      = "Third parameter shall be provided to clarify if types safety important in this context";
    private static final String strProblemSafeToMakeStrict = "Safely place true as 3rd parameter (strict mode)";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    private static final HashSet<String> functionsSet = new HashSet<String>();
    static {
        functionsSet.add("array_search");
        functionsSet.add("in_array");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                final String strFunction    = reference.getName();
                if (intArgumentsCount != 2 || StringUtil.isEmpty(strFunction)) {
                    return;
                }

                if (functionsSet.contains(strFunction)) {
                    String strMessage = strProblemDescription;
                    if (reference.getParameters()[0] instanceof StringLiteralExpression) {
                        strMessage = strProblemSafeToMakeStrict;
                    }

                    holder.registerProblem(reference, strMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
