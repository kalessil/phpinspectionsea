package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.utils.MethodIdentityUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ExecUsageStrategy {
    private static final String message = "'PDO::exec(...)' should be used instead (consumes less resources).";

    public static void apply(@NotNull MethodReference reference, @NotNull final ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        final String methodName   = reference.getName();
        if (params.length != 1 || methodName == null || !methodName.equals("query")) {
            return;
        }

        if (
            reference.getParent().getClass() == StatementImpl.class &&
            MethodIdentityUtil.isReferencingMethod(reference, "\\PDO", "query")
        ) {
            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseExecFix());
        }
    }

    private static class UseExecFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use '->exec(...)' instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof MethodReference) {
                ((MethodReference) expression).handleElementRename("exec");
            }
        }
    }
}
