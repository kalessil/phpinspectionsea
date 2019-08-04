package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
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

public class ShortOpenTagUsageInspector extends PhpInspection {
    private static final String message = "Using the '<?' short tag considered to be a bad practice";

    @NotNull
    @Override
    public String getShortName() {
        return "ShortOpenTagUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PHP short open tag usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpGroupStatement(@NotNull GroupStatement groupStatement) {
                if (this.shouldSkipAnalysis(groupStatement, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                final PsiElement last = groupStatement.getLastChild();
                if (last instanceof LeafPsiElement) {
                    this.analyze((LeafPsiElement) last);
                }
            }

            @Override
            public void visitWhiteSpace(@NotNull PsiWhiteSpace space) {
                if (this.shouldSkipAnalysis(space, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                final PsiElement previous = space.getPrevSibling();
                if (previous instanceof LeafPsiElement) {
                    this.analyze((LeafPsiElement) previous);
                }
            }

            private void analyze(@NotNull LeafPsiElement candidate) {
                if (candidate.getElementType() == PhpTokenTypes.PHP_OPENING_TAG && candidate.getText().equals("<?")) {
                    holder.registerProblem(candidate, message, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use '<?php' instead";

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
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof LeafPsiElement && !project.isDisposed()) {
                expression.replace(PhpPsiElementFactory.createFromText(project, PhpTokenTypes.PHP_OPENING_TAG, "<?php"));
            }
        }
    }
}
