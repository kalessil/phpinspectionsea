package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnNecessaryDoubleQuotesInspector extends BasePhpInspection {
    private static final String message = "Safely use single quotes instead.";

    @NotNull
    public String getShortName() {
        return "UnNecessaryDoubleQuotesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpStringLiteralExpression(@NotNull StringLiteralExpression expression) {
                /* skip processing single-quoted and strings with injections */
                if (expression.isSingleQuote() || expression.isHeredoc() || expression.getFirstPsiChild() != null) {
                    return;
                }
                /* annotation is doc-blocks must not be analyzed */
                if (ExpressionSemanticUtil.getBlockScope(expression) instanceof PhpDocComment) {
                    return;
                }

                /* literals with escape sequences must not be analyzed */
                /* note: don't use PhpStringUtil.unescapeText as it simply strips escape sequences */
                final String contents = expression.getContents().replaceAll("\\\\\\$", "\\$").replaceAll("\\\\\"", "\"");
                if (contents.indexOf('\'') >= 0 || contents.contains("\\")) {
                    return;
                }

                holder.registerProblem(expression, message, new TheLocalFix());
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Replace with single quotes";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement literal = descriptor.getPsiElement();
            if (literal instanceof StringLiteralExpression && !project.isDisposed()) {
                final String rawText        = ((StringLiteralExpression) literal).getContents();
                final String unescapedText  = PhpStringUtil.unescapeText(rawText, false);
                final String textExpression = "'" + PhpStringUtil.escapeText(unescapedText, true) + "'";

                final PhpPsiElement replacement
                    = PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, textExpression);
                literal.replace(replacement);
            }
        }
    }
}
