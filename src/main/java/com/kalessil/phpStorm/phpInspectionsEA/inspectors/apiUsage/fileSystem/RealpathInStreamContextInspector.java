package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class RealpathInStreamContextInspector extends BasePhpInspection {
    private static final String messageUseDirname = "'realpath(...)' works differently in a stream context (e.g., for phar://...). Consider using 'dirname(...)' instead.";
    private static final String patternUseDirname = "'%s' should be used instead (due to how realpath handles streams).";

    @NotNull
    @Override
    public String getShortName() {
        return "RealpathInStreamContextInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Phar-incompatible 'realpath(...)' usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
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
                    if (replacement != null) {
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(String.format(patternUseDirname, replacement)),
                                new SecureRealpathFix(replacement)
                        );
                    } else {
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(messageUseDirname)
                        );
                    }
                    return;
                }

                /* case 2: realpath applied to a relative path '..' */
                for (final StringLiteralExpression literal : PsiTreeUtil.findChildrenOfType(reference, StringLiteralExpression.class)) {
                    if (literal.getContents().contains("..")) {
                        final String replacement = this.generateReplacement(subject);
                        if (replacement != null) {
                            holder.registerProblem(
                                    reference,
                                    MessagesPresentationUtil.prefixWithEa(String.format(patternUseDirname, replacement)),
                                    new SecureRealpathFix(replacement)
                            );
                        } else {
                            holder.registerProblem(
                                    reference,
                                    MessagesPresentationUtil.prefixWithEa(messageUseDirname)
                            );
                        }
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        SecureRealpathFix(@NotNull String expression) {
            super(expression);
        }
    }
}