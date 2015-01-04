package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class AmbiguousMemberInitializationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Initialization can be omitted, as null " +
            "initialization is applied by default";

    @NotNull
    public String getDisplayName() {
        return "API: ambiguous class field initialization";
    }

    @NotNull
    public String getShortName() {
        return "AmbiguousMemberInitializationInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field field) {
                PsiElement objDefaultValue = field.getDefaultValue();
                if (
                    !(objDefaultValue instanceof ConstantReference) ||
                    ((ConstantReference) objDefaultValue).getType() != PhpType.NULL
                ) {
                    return;
                }

                holder.registerProblem(objDefaultValue, strProblemDescription, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
            }
        };
    }
}

