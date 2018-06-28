package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpShellCommandExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BacktickOperatorUsageInspector extends BasePhpInspection {
    private static final String message = "Prefer using 'shell_exec(...)' instead (security analysis friendly).";

    @NotNull
    public String getShortName() {
        return "BacktickOperatorUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpShellCommand(@NotNull PhpShellCommandExpression expression) {
                final String raw = expression.getText();
                if (raw.length() > 2) {
                    final String command     = raw.substring(1, raw.length() - 1).replaceAll("\\\\`", "`");
                    final String replacement = String.format("shell_exec(\"%s\")", PhpStringUtil.escapeText(command, false));
                    holder.registerProblem(expression, message, new UseShellExecFix(replacement));
                }
            }
        };
    }

    private static final class UseShellExecFix extends UseSuggestedReplacementFixer {
        private static final String title = "Replace with shell_exec()";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseShellExecFix(@NotNull String expression) {
            super(expression);
        }
    }
}
