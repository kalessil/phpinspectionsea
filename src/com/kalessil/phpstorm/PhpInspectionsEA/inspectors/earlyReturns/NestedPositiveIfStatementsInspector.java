package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.earlyReturns;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.inspections.PhpInspection;

import org.jetbrains.annotations.NotNull;

public class NestedPositiveIfStatementsInspector extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return null;
    }
}