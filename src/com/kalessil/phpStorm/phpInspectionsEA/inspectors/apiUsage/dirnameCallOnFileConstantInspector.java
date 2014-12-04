package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class dirnameCallOnFileConstantInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Can be replaced with __DIR__ constant";

    @NotNull
    public String getDisplayName() {
        return "API: outdated __DIR__ equivalent";
    }

    @NotNull
    public String getShortName() {
        return "dirnameCallOnFileConstantInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final int intArgumentsCount = reference.getParameters().length;
                if (intArgumentsCount != 1) {
                    return;
                }

                final String strFunction = reference.getName();
                if (null == strFunction) {
                    return;
                }

                /** since we already checked amount of arguments, lets reverse regular check logic */
                PsiElement objFirstParameter = reference.getParameters()[0];
                final boolean isFileConstantPassed = (
                    objFirstParameter instanceof ConstantReference &&
                    objFirstParameter.getText().toUpperCase().equals("__FILE__")
                );
                /** if pre-conditions are not met, don't test function name */
                final boolean isTargetFunction = (
                    isFileConstantPassed &&
                    strFunction.toLowerCase().equals("dirname")
                );
                if (!isTargetFunction) {
                    return;
                }

                holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}
