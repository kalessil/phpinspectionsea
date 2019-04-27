package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class UnnecessaryUseAliasInspector extends BasePhpInspection {
    private static final String messageAlias  = "' as %s' is redundant here.";
    private static final String messageImport = "The symbol is imported twice, consider dropping this import.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryUseAliasInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpUse(@NotNull PhpUse expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                if (!expression.isTraitImport()) {
                    final String alias = expression.getAliasName();
                    if (alias != null && !alias.isEmpty()) {
                        final String symbol = expression.getFQN();
                        if (symbol.endsWith('\\' + alias)) {
                            holder.registerProblem(
                                    expression.getLastChild(),
                                    String.format(messageAlias, alias),
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                    new TheLocalFix()
                            );
                        } else {
                            final PsiFile file = expression.getContainingFile();
                            if (file instanceof PhpFile) {
                                for (final PsiElement definition : ((PhpFile) file).getTopLevelDefs().values()) {
                                    if (definition instanceof PhpUse && ((PhpUse) definition).getFQN().equals(symbol)) {
                                        if (definition != expression) {
                                            holder.registerProblem(expression.getFirstChild(), messageImport);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
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
