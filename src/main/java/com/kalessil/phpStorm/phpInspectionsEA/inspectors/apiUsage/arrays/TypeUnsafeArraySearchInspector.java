package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String message = "Third parameter shall be provided to clarify if types safety important in this context";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    private static final Set<String> functionsSet = new HashSet<>();
    static {
        functionsSet.add("array_search");
        functionsSet.add("in_array");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length != 2 || StringUtil.isEmpty(functionName) || !functionsSet.contains(functionName)) {
                    return;
                }

                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
