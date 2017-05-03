package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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

        final PsiElement parent = reference.getParent();
        if (parent instanceof StatementImpl) {
            final PsiElement resolved = reference.resolve();
            if (resolved != null && isPdoQueryMethod((Method) resolved)) {
                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseExecFix());
            }
        }
    }

    private static boolean isPdoQueryMethod(@NotNull Method method) {
        boolean result = method.getFQN().equals("\\PDO::query");
        if (!result) {
            final PhpClass clazz = method.getContainingClass();
            if (clazz != null && !clazz.isTrait()) {
                final Set<PhpClass> parents = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(clazz, true);
                for (final PhpClass parent : parents) {
                    if (parent.getFQN().equals("\\PDO")) {
                        result = true;
                        break;
                    }
                }
                parents.clear();
            }
        }

        return result;
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
