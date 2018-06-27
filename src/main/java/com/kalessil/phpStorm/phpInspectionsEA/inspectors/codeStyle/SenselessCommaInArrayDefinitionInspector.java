package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
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

public class SenselessCommaInArrayDefinitionInspector extends BasePhpInspection {
    private static final String message = "Can be safely dropped. The comma will be ignored by PHP.";

    @NotNull
    public String getShortName() {
        return "SenselessCommaInArrayDefinitionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                final PsiElement last      = expression.getLastChild().getPrevSibling();
                final PsiElement candidate = last instanceof PsiWhiteSpace ? last.getPrevSibling() : last;
                if (OpenapiTypesUtil.is(candidate, PhpTokenTypes.opCOMMA)) {
                    holder.registerProblem(candidate, message, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Safely drop comma";

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
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                target.delete();
            }
        }
    }
}
