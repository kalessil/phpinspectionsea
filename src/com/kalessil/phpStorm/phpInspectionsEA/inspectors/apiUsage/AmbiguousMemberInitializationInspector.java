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
    private static final String strProblemDescription = "Null assignment can be safely removed. Define null in annotations if it's important";

    @NotNull
    public String getShortName() {
        return "AmbiguousMemberInitializationInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field field) {
                if (field.isConstant()) {
                    return;
                }

                PsiElement objDefaultValue = field.getDefaultValue();
                if (objDefaultValue instanceof ConstantReference && PhpType.NULL == ((ConstantReference) objDefaultValue).getType()) {
                    holder.registerProblem(objDefaultValue, strProblemDescription, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
        };
    }
}

