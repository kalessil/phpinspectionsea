package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CallableReferenceNameMismatchInspector extends BasePhpInspection {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static final String messagePattern     = "Name provided in this call should be '%n%' (case mismatch).";

    @NotNull
    public String getShortName() {
        return "CallableReferenceNameMismatchInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                inspectCaseIdentity(reference, false);
            }
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* invoke caching as (assumption) in 99% of cases functions are global; assumption was right ;) */
                inspectCaseIdentity(reference, true);
            }

            private void inspectCaseIdentity(@NotNull FunctionReference reference, boolean useCache) {
                /* StringUtil is not used due to performance optimization */
                final String usedName = reference.getName();
                if (null == usedName || 0 == usedName.length() || (useCache && cache.containsKey(usedName))) {
                    return;
                }

                /* resolve callable and ensure the case matches */
                final PsiElement resolved = reference.resolve();
                if (resolved instanceof Function) {
                    final Function function = (Function) resolved;
                    final String realName   = function.getName();

                    /* cache root NS functions if caching was requested */
                    if (useCache && function.getFQN().equals('\\' + realName)) {
                        cache.putIfAbsent(realName, realName);
                    }

                    if (!realName.equals(usedName) && realName.equalsIgnoreCase(usedName)) {
                        /* report issues found */
                        final String message = messagePattern.replace("%n%", realName);
                        holder.registerProblem(reference, message, new CallableReferenceNameMismatchQuickFix());
                    }
                }
            }
        };
    }

    private static class CallableReferenceNameMismatchQuickFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Fix case mismatch";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof FunctionReference) {
                final FunctionReference reference = (FunctionReference) target;
                final PsiElement callable         = reference.resolve();
                if (callable instanceof Function) {
                    reference.handleElementRename(((Function) callable).getName());
                }
            }
        }
    }
}
