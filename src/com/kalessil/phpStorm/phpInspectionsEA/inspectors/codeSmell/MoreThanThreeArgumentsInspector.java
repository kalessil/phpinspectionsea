package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class MoreThanThreeArgumentsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "No more than three arguments recommended";

    @NotNull
    public String getShortName() {
        return "MoreThanThreeArgumentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                this.visitPhpFunction(method);
            }

            public void visitPhpFunction(Function function) {
                final PsiElement functionName   = function.getNameIdentifier();
                final boolean isValidIdentifier = functionName != null && !StringUtil.isEmpty(functionName.getText());
                if (isValidIdentifier && function.getParameters().length > 3) {
                    holder.registerProblem(functionName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}