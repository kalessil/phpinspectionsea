package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
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
            public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {
                String valueWithQuotes = expression.getText();
                if (
                    valueWithQuotes.charAt(0) != '"' ||
                    valueWithQuotes.indexOf('$') > 0 ||
                    valueWithQuotes.indexOf('\\') > 0 ||
                    valueWithQuotes.indexOf('\'') > 0
                ) {
                    return;
                }

                /* annotation is doc-blocks must not be analyzed */
                if (!(ExpressionSemanticUtil.getBlockScope(expression) instanceof PhpDocComment)) {
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Replace with single quotes";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement target = descriptor.getPsiElement();
            if (target instanceof StringLiteralExpression) {
                String unescaped      = PhpStringUtil.unescapeText(((StringLiteralExpression) target).getContents(), true);
                String textExpression = '\'' + PhpStringUtil.escapeText(unescaped, true, '\n', '\t') + '\'';

                PhpPsiElement replacement = PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, textExpression);
                target.replace(replacement);
            }
        }
    }
}
