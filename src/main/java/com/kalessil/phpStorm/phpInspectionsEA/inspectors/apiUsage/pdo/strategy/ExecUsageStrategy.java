package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.utils.MethodIdentityUtil;
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

final public class ExecUsageStrategy {
    private static final String message = "'PDO::exec(...)' should be used instead (consumes less resources).";

    public static void apply(@NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final String methodName = reference.getName();
        if (methodName != null && methodName.equals("query")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 1) {
                final boolean isTarget =
                    OpenapiTypesUtil.isStatementImpl(reference.getParent()) &&
                    MethodIdentityUtil.isReferencingMethod(reference, "\\PDO", "query");
                if (isTarget) {
                    holder.registerProblem(reference, message, new UseExecFix());
                }
            }
        }
    }

    private static final class UseExecFix implements LocalQuickFix {
        private static final String title = "Use '->exec(...)' instead";

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
            if (expression instanceof MethodReference) {
                ((MethodReference) expression).handleElementRename("exec");
            }
        }
    }
}
