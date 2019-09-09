package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessarySemicolonInspector extends PhpInspection {
    private static final String message = "Unnecessary semicolon.";

    @NotNull
    @Override
    public String getShortName() {
        return "UnnecessarySemicolonInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unnecessary semicolon";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpStatement(@NotNull Statement statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                final boolean isBlade = holder.getFile().getName().endsWith(".blade.php");
                if (!isBlade && statement.getChildren().length == 0) {
                    final PsiElement parent = statement.getParent();
                    if (
                        parent instanceof If || parent instanceof ElseIf || parent instanceof Else ||
                        OpenapiTypesUtil.isLoop(parent) ||
                        OpenapiTypesUtil.is(parent.getFirstChild(), PhpTokenTypes.kwDECLARE)
                    ) {
                        return;
                    }

                    holder.registerProblem(statement, message, new DropUnnecessarySemicolonFix());
                }
            }

            @Override
            public void visitPhpEchoStatement(@NotNull PhpEchoStatement echo) {
                if (this.shouldSkipAnalysis(echo, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                if (!OpenapiTypesUtil.is(echo.getFirstChild(), PhpTokenTypes.kwECHO)) {
                    final PsiElement last = echo.getLastChild();
                    if (OpenapiTypesUtil.is(last, PhpTokenTypes.opSEMICOLON)) {
                        holder.registerProblem(last, message, new DropUnnecessarySemicolonFix());
                    }
                }
            }
        };
    }

    private static final class DropUnnecessarySemicolonFix implements LocalQuickFix {
        private static final String title = "Drop unnecessary semicolon";

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
            if (expression != null && !project.isDisposed()) {
                final PsiElement previous = expression.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous.delete();
                }
                expression.delete();
            }
        }
    }
}

