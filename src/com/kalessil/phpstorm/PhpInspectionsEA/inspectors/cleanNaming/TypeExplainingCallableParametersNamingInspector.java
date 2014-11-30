package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.cleanNaming;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class TypeExplainingCallableParametersNamingInspector extends BasePhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return null;
    }
}

