package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Positive and negative variants can be skipped: condition already returns boolean";

    @NotNull
    public String getDisplayName() {
        return "API: ternary operator simplification";
    }

    @NotNull
    public String getShortName() {
        return "TernaryOperatorSimplifyInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PsiElement objTrueVariant = expression.getTrueVariant();
                final PsiElement objFalseVariant = expression.getFalseVariant();
                if (!(objTrueVariant instanceof ConstantReference) || !(objFalseVariant instanceof ConstantReference)) {
                    return;
                }


                final boolean isTrueVariantBoolean = ExpressionSemanticUtil.isBoolean((ConstantReference) objTrueVariant);
                /** skip false variant test if true one already did not meet pre-conditions */
                final boolean isFalseVariantBoolean = (
                    isTrueVariantBoolean &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) objFalseVariant)
                );
                if (!isFalseVariantBoolean) {
                    return;
                }

                final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                /** or maybe try resolving type when not on-the-fly analysis is running */
                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}