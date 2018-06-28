package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.ElseIf;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class UnnecessarySemicolonInspector extends BasePhpInspection {
    private static final String message = "Unnecessary semicolon.";

    @NotNull
    public String getShortName() {
        return "UnnecessarySemicolonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpStatement(@NotNull Statement statement) {
                if (statement.getChildren().length == 0) {
                    final PsiElement parent = statement.getParent();
                    if (
                        parent instanceof If || parent instanceof ElseIf || parent instanceof Else ||
                        OpenapiTypesUtil.isLoop(parent) ||
                        OpenapiTypesUtil.is(parent.getFirstChild(), PhpTokenTypes.kwDECLARE)
                    ) {
                        return;
                    }

                    holder.registerProblem(statement, message, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Drop it";

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
            if (expression instanceof Statement && !project.isDisposed()) {
                /* drop preceding space */
                final PsiElement previous = expression.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous.delete();
                }
                expression.delete();
            }
        }
    }
}

