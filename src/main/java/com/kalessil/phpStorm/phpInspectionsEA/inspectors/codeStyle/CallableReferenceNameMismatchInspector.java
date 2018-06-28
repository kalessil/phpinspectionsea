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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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
                final String methodName = reference.getName();
                if (methodName != null && !methodName.isEmpty()) {
                    this.inspectCaseIdentity(reference, methodName, false);
                }
            }
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && !functionName.isEmpty() && !cache.containsKey(functionName)) {
                    this.inspectCaseIdentity(reference, functionName, true);
                }
            }

            private void inspectCaseIdentity(@NotNull FunctionReference reference, @NotNull String referenceName, boolean useCache) {
                /* resolve callable and ensure the case matches */
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof Function) {
                    final Function function = (Function) resolved;
                    final String realName   = function.getName();
                    if (useCache && function.getFQN().equals('\\' + realName)) {
                        cache.putIfAbsent(realName, realName);
                    }
                    if (!referenceName.equals(realName) && referenceName.equalsIgnoreCase(realName)) {
                        holder.registerProblem(
                                reference,
                                messagePattern.replace("%n%", realName),
                                new CallableReferenceNameMismatchQuickFix()
                        );
                    }
                }
            }
        };
    }

    private static final class CallableReferenceNameMismatchQuickFix implements LocalQuickFix {
        private static final String title = "Fix case mismatch";

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
            if (target instanceof FunctionReference && !project.isDisposed()) {
                final FunctionReference reference = (FunctionReference) target;
                final PsiElement callable         = OpenapiResolveUtil.resolveReference(reference);
                if (callable instanceof Function) {
                    reference.handleElementRename(((Function) callable).getName());
                }
            }
        }
    }
}
