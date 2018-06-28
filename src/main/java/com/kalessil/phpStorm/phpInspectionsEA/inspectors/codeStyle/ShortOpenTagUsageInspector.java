package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ShortOpenTagUsageInspector extends BasePhpInspection {
    private static final String message = "Using the '<?' short tag considered to be a bad practice";

    @NotNull
    public String getShortName() {
        return "ShortOpenTagUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitWhiteSpace(PsiWhiteSpace space) {
                if (space.getPrevSibling() instanceof LeafPsiElement) {
                    final LeafPsiElement tag = (LeafPsiElement) space.getPrevSibling();
                    if (PhpTokenTypes.PHP_OPENING_TAG == tag.getElementType() && tag.getText().equals("<?")) {
                        holder.registerProblem(tag, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                    }
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
            if (expression instanceof LeafPsiElement) {
                expression.replace(PhpPsiElementFactory.createFromText(project, PhpTokenTypes.PHP_OPENING_TAG, "<?php"));
            }
        }
    }
}
