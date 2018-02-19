package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.PhpShellCommandExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

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
    static final String message = "Prefer using 'shell_exec(...)' instead (security analysis friendly).";

    @NotNull
    public String getShortName() {
        return "BacktickOperatorUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpShellCommand(@NotNull final PhpShellCommandExpression shellCommandExpression) {
                holder.registerProblem(shellCommandExpression, message, new UseShellExecQuickFix(shellCommandExpression));
            }
        };
    }

    private static class UseShellExecQuickFix implements LocalQuickFix {
        private static final Pattern REGEXP_UNESCAPE_BACKTICKS = Pattern.compile("\\\\`");

        private final SmartPsiElementPointer<PhpShellCommandExpression> shellCommandExpressionPointer;

        UseShellExecQuickFix(@NotNull final PhpShellCommandExpression shellCommandExpression) {
            final SmartPointerManager factory = SmartPointerManager.getInstance(shellCommandExpression.getProject());
            shellCommandExpressionPointer = factory.createSmartPsiElementPointer(shellCommandExpression);
        }

        @NotNull
        @Override
        public String getName() {
            return getFamilyName();
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "Replace with shell_exec()";
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor problemDescriptor) {
            final PhpShellCommandExpression shellCommandExpression = shellCommandExpressionPointer.getElement();

            if (shellCommandExpression == null) {
                return;
            }

            final String shellCommandText              = shellCommandExpression.getText();
            final String shellCommandTextCutted        = shellCommandText.substring(1, shellCommandText.length() - 1);
            final String shellCommandBacktickUnescaped = REGEXP_UNESCAPE_BACKTICKS.matcher(shellCommandTextCutted).replaceAll("`");
            final String shellCommandQuoteEscaped      = PhpStringUtil.escapeText(shellCommandBacktickUnescaped, false);

            final PsiElement shellExecFunctionReference = PhpPsiElementFactory
                .createPhpPsiFromText(project, ParenthesizedExpression.class, "(shell_exec(\"" + shellCommandQuoteEscaped + "\"))")
                .getArgument();

            if (shellExecFunctionReference != null) {
                shellCommandExpression.replace(shellExecFunctionReference);
            }
        }
    }
}
