package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessaryUseAliasInspector extends BasePhpInspection {
    private static final String messagePattern = "' as %a%' is redundant here.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryUseAliasInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUse(PhpUse expression) {
                if (expression.isTraitImport()) {
                    return;
                }

                final String alias = expression.getAliasName();
                if (!StringUtil.isEmpty(alias) && expression.getFQN().endsWith("\\" + alias)) {
                    final String message = messagePattern.replace("%a%", alias);
                    holder.registerProblem(expression.getLastChild(), message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove alias";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof PhpUse) {
                expression.getLastChild().delete(); // alias itself
                expression.getLastChild().delete(); // "as" keyword
            }
        }
    }
}
