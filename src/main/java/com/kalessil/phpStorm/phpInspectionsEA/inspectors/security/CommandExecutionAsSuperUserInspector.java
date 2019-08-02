package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpShellCommandExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CommandExecutionAsSuperUserInspector extends LocalInspectionTool {
    private static final String message = "Applications must not require super user privileges, please find another solution.";

    @NotNull
    @Override
    public String getShortName() {
        return "CommandExecutionAsSuperUserInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpStringLiteralExpression(@NotNull StringLiteralExpression literal) {
                if (this.shouldSkipAnalysis(literal, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String content = literal.getContents();
                if (content.length() >= 3) {
                    final boolean isTarget = content.startsWith("su ") || content.startsWith("sudo ");
                    if (isTarget) {
                        holder.registerProblem(literal, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }

            @Override
            public void visitPhpShellCommand(@NotNull PhpShellCommandExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String content = expression.getText();
                if (content.length() >= 5) {
                    final String command   = content.substring(1, content.length() - 1);
                    final boolean isTarget = command.startsWith("su ") || command.startsWith("sudo ");
                    if (isTarget) {
                        holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}
