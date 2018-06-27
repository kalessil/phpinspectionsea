package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryUseAliasInspector extends BasePhpInspection {
    private static final String messagePattern = "' as %s' is redundant here.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryUseAliasInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpUse(@NotNull PhpUse expression) {
                if (!expression.isTraitImport()) {
                    final String alias = expression.getAliasName();
                    if (alias != null && !alias.isEmpty() && expression.getFQN().endsWith('\\' + alias)) {
                        holder.registerProblem(
                                expression.getLastChild(),
                                String.format(messagePattern, alias),
                                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                new TheLocalFix()
                        );
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Remove unnecessary alias";

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
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof PhpUse && !project.isDisposed()) {
                expression.getLastChild().delete(); // alias itself
                expression.getLastChild().delete(); // "as" keyword
            }
        }
    }
}
