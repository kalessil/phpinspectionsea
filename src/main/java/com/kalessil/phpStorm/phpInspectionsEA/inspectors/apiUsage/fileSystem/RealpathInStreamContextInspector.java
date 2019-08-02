package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class RealpathInStreamContextInspector extends PhpInspection {
    private static final String messageUseDirname = "'realpath(...)' works differently in a stream context (e.g., for phar://...). Consider using 'dirname(...)' instead.";
    private static final String patternUseDirname = "'%s' should be used instead (due to how realpath handles streams).";

    @NotNull
    @Override
    public String getShortName() {
        return "RealpathInStreamContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("realpath")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && !this.isTestContext(reference)) {
                        this.analyze(reference, arguments[0]);
                    }
                }
            }

            private void analyze(@NotNull FunctionReference reference, @NotNull PsiElement subject) {
                /* case 1: include/require context */
                PsiElement parent = reference.getParent();
                while (parent instanceof ParenthesizedExpression) {
                    parent = parent.getParent();
                }
                if (parent instanceof Include) {
                    final String replacement = this.generateReplacement(subject);
                    holder.registerProblem(
                            reference,
                            replacement == null ? messageUseDirname : String.format(patternUseDirname, replacement),
                            replacement == null ? null : new SecureRealpathFix(replacement)
                    );
                    return;
                }

                /* case 2: realpath applied to a relative path '..' */
                for (final StringLiteralExpression literal : PsiTreeUtil.findChildrenOfType(reference, StringLiteralExpression.class)) {
                    if (literal.getContents().contains("..")) {
                        final String replacement = this.generateReplacement(subject);
                        holder.registerProblem(
                                reference,
                                replacement == null ? messageUseDirname : String.format(patternUseDirname, replacement),
                                replacement == null ? null : new SecureRealpathFix(replacement)
                        );
                        break;
                    }
                }
            }

            @Nullable
            private String generateReplacement(@NotNull PsiElement subject) {
                String result = null;

                if (subject instanceof ConcatenationExpression) {
                    final ConcatenationExpression concat = (ConcatenationExpression) subject;
                    final PsiElement left                = concat.getLeftOperand();
                    final PsiElement right               = concat.getRightOperand();
                    if (left != null && !(left instanceof ConcatenationExpression) && right instanceof StringLiteralExpression) {
                        final StringLiteralExpression literal = (StringLiteralExpression) right;
                        final String contents                 = literal.getContents();
                        final String quote                    = literal.isSingleQuote() ? "'" : "\"";
                        if (contents.startsWith("/..")) {
                            final StringBuilder newLeft = new StringBuilder(left.getText());
                            String newRight             = contents;
                            while (newRight.startsWith("/..")) {
                                newRight = newRight.replaceFirst("/\\.\\.", "");
                                newLeft.insert(0, "dirname(").append(')');
                            }
                            result = newLeft + " . " + quote + newRight + quote;
                        }
                    }
                }

                if (subject instanceof StringLiteralExpression) {
                    result = subject.getText();
                }

                return result;
            }
        };
    }

    private static final class SecureRealpathFix extends UseSuggestedReplacementFixer {
        private static final String title = "Secure this realpath(...)";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        SecureRealpathFix(@NotNull String expression) {
            super(expression);
        }
    }
}