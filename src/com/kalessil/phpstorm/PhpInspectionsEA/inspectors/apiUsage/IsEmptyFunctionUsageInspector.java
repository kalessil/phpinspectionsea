package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;

import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.PhpEmpty;

import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    public static final String strProblemDescription =
            "'empty(...)' is not type safe and brings N-path complexity due to multiple types supported." +
            " Consider refactoring this code.";

    @NotNull
    public String getDisplayName() {
        return "'empty(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpEmpty(PhpEmpty emptyExpression) {
                holder.registerProblem(emptyExpression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
