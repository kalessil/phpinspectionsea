package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayTypeOfParameterByDefaultValueInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Types safety: declare parameter as 'array' type";

    @NotNull
    public String getDisplayName() {
        return "Types safety: parameter can be declared as array";
    }

    @NotNull
    public String getShortName() {
        return "ArrayTypeOfParameterByDefaultValueInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpParameter(Parameter parameter) {
                if (!parameter.isOptional()) {
                    return;
                }

                PsiElement objDefaultValue = parameter.getDefaultValue();
                if (!(objDefaultValue instanceof ArrayCreationExpression)) {
                    return;
                }

                if (!parameter.getDeclaredType().isEmpty()) {
                    return;
                }

                holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}