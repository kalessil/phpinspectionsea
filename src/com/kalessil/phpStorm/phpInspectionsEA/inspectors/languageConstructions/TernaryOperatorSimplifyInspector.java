package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.*;

import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class TernaryOperatorSimplifyInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This statement can be simplified";

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
                if (
                    !(objTrueVariant instanceof ConstantReference) ||
                    !(objFalseVariant instanceof ConstantReference)
                ) {
                    return;
                }


                final boolean isTrueVariantBoolean = (
                    PhpLangUtil.isTrue((ConstantReference) objTrueVariant) ||
                    PhpLangUtil.isFalse((ConstantReference) objTrueVariant)
                );
                /** skip false variant test if true one already did not meet pre-conditions */
                final boolean isFalseVariantBoolean = (
                    isTrueVariantBoolean && (
                        PhpLangUtil.isTrue((ConstantReference) objFalseVariant) ||
                        PhpLangUtil.isFalse((ConstantReference) objFalseVariant)
                    )
                );
                if (!isFalseVariantBoolean) {
                    return;
                }

                final PsiElement objCondition = expression.getCondition();
                final boolean isBinaryExpressionInCondition = (
                    objCondition instanceof BinaryExpression ||
                    (
                        objCondition instanceof ParenthesizedExpression &&
                        ((ParenthesizedExpression) objCondition).getArgument() instanceof BinaryExpression
                    )
                );
                if (!isBinaryExpressionInCondition) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}