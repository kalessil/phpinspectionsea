package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class ForeachOnArrayComponentsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Classic foreach construction can be uses (can trigger unused variable warning)";

    @NotNull
    public String getShortName() {
        return "ForeachOnArrayComponentsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                PsiElement objArray = ExpressionSemanticUtil.getExpressionTroughParenthesis(foreach.getArray());
                /** verify structure */
                if (objArray instanceof FunctionReference && null == foreach.getKey()) {
                    String strFunction = ((FunctionReference) objArray).getName();
                    if (null != strFunction && (strFunction.equals("array_keys") || strFunction.equals("array_values"))) {
                        holder.registerProblem(foreach.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
