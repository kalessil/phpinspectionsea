package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    private static final String strProblemDescription =
            "'isset(...)' returns true when key is present and associated with null value. " +
            "'array_key_exists(...)' construction can be used instead.";
    private static final String strProblemDescriptionUseNullComparison = "Use 'null === $...' construction instead";


    @NotNull
    public String getDisplayName() {
        return "API: 'isset' instead of 'array_key_exists'";
    }

    @NotNull
    public String getShortName() {
        return "UnSafeIsSetOverArrayInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIsset(PhpIsset issetExpression) {
                for (PsiElement parameter : issetExpression.getVariables()) {
                    parameter = ExpressionSemanticUtil.getExpressionTroughParenthesis(parameter);

                    if (parameter instanceof ArrayAccessExpression) {
                        holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    } else {
                        holder.registerProblem(parameter, strProblemDescriptionUseNullComparison, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}