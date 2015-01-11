package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;


import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class ElvisOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String strProblemDescription = "' ... ?: ...' construction shall be used instead";

    @NotNull
    public String getDisplayName() {
        return "Performance: elvis operator can be used";
    }

    @NotNull
    public String getShortName() {
        return "ElvisOperatorCanBeUsedInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                /** construction requirements */
                final PsiElement objTrueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                if (null == objTrueVariant) {
                    return;
                }
                final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (null == objCondition) {
                    return;
                }

                /** if true variant is the object or expressions are not equals */
                if (objCondition != objTrueVariant && PsiEquivalenceUtil.areElementsEquivalent(objCondition, objTrueVariant)) {
                    holder.registerProblem(objTrueVariant, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
