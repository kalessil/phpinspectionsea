package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return null;
    }
}