package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String message = "Positive and negative variants can be skipped: the condition already returns a boolean";

    @NotNull
    public String getShortName() {
        return "TernaryOperatorSimplifyInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PsiElement trueVariant  = expression.getTrueVariant();
                final PsiElement falseVariant = expression.getFalseVariant();
                /* if both variants are identical, senseless ternary operator will spot it */
                if (!PhpLanguageUtil.isBoolean(trueVariant) || !PhpLanguageUtil.isBoolean(falseVariant)) {
                    return;
                }

                PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                /* TODO: resolve type of other expressions */
                if (condition instanceof BinaryExpression) {
                    holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}