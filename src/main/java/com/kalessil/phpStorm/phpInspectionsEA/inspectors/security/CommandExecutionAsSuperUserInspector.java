package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import org.jetbrains.annotations.NotNull;

public class CommandExecutionAsSuperUserInspector extends LocalInspectionTool {

    @NotNull
    public String getShortName() {
        return "CommandExecutionAsSuperUser";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            // "su(do)? (-u)?"
        };
    }
}
