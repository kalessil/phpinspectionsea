package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;

import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class MoreThanThreeArgumentsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Clean code: too much arguments, not more than 3 recommended.";

    @NotNull
    public String getDisplayName() {
        return "Clean code: amount of parameters up to 3";
    }

    @NotNull
    public String getShortName() {
        return "MoreThanThreeArgumentsInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                PsiElement objFunctionName = method.getNameIdentifier();
                if (objFunctionName != null && method.getParameters().length > 3) {
                    holder.registerProblem(objFunctionName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }

            public void visitPhpFunction(Function function) {
                PsiElement objFunctionName = function.getNameIdentifier();
                if (objFunctionName != null && function.getParameters().length > 3) {
                    holder.registerProblem(objFunctionName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}