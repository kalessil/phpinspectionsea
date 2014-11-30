package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;

import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    public static final String strProblemDescription =
            "'isset(...)' can produce issues due to null values handling " +
            "consider using 'array_key_exists(...)' instead.";

    @NotNull
    public String getDisplayName() {
        return "'isset(...)' instead of 'array_key_exists(...)' on array item";
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
                    if (!(parameter instanceof ArrayAccessExpression)) {
                        continue;
                    }

                    holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}