package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ProblemHighlightType;

import com.jetbrains.php.lang.psi.elements.FunctionReference;

import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    public static final String strProblemDescription = "'is_null(...)' shall be replace with '... === null'";

    @NotNull
    public String getDisplayName() {
        return "'is_null(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
            final String strFunctionName = reference.getName();
            if (strFunctionName == null) {
                return;
            }

            final boolean isNullFunctionUsed = ("is_null").equals(strFunctionName.toLowerCase());
            if (!isNullFunctionUsed) {
                return;
            }

            holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}