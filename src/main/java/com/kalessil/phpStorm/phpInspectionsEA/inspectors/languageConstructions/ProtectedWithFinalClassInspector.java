package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class ProtectedWithFinalClassInspector extends BasePhpInspection {
    private static final String message = "Protected modifier could be replaced by private.";

    @NotNull
    public String getShortName() {
        return "ProtectedWithFinalClassInspector";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpField(final Field field) {
                if (!field.getModifier().isProtected()) {
                    return;
                }

                final PhpClass fieldClass = field.getContainingClass();

                assert fieldClass != null;

                if (!fieldClass.isFinal()) {
                    return;
                }

                final PhpModifierList fieldProtectedModifier = PsiTreeUtil.findChildOfType(field.getParent(), PhpModifierList.class);

                assert fieldProtectedModifier != null;

                problemsHolder.registerProblem(fieldProtectedModifier, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
