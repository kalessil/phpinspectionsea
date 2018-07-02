package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

public class InconsistentQueryBuildInspector extends BasePhpInspection {
    private static final String messagePattern = "'ksort(%a%, SORT_STRING)' should be used instead, so http_build_query() produces result independent from key types.";

    @NotNull
    public String getShortName() {
        return "InconsistentQueryBuildInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String function = reference.getName();
                if (function != null && function.equals("ksort")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        /* pre-condition satisfied, now check if http_build_query used in the scope */
                        final Function scope = ExpressionSemanticUtil.getScope(reference);
                        if (scope != null) {
                            for (final FunctionReference call : PsiTreeUtil.findChildrenOfType(scope, FunctionReference.class)) {
                                /* skip inspected call and calls without arguments */
                                if (call == reference || !OpenapiTypesUtil.isFunctionReference(call)) {
                                    continue;
                                }
                                /* skip non-target function */
                                final String callFunctionName = call.getName();
                                if (callFunctionName == null || !callFunctionName.equals("http_build_query")) {
                                    continue;
                                }
                                final PsiElement[] callArguments = call.getParameters();
                                if (callArguments.length == 0) {
                                    continue;
                                }

                                /* pattern match: ksort and http_build_query operating on the same expression */
                                if (OpenapiEquivalenceUtil.areEqual(callArguments[0], arguments[0])) {
                                    final String message = messagePattern.replace("%a%", arguments[0].getText());
                                    holder.registerProblem(reference, message, new TheLocalFix());

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Add SORT_STRING as an argument";

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
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                final FunctionReference call = (FunctionReference) expression;
                final PsiElement[] params    = call.getParameters();

                /* override existing parameters */
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "ksort(null, SORT_STRING)");
                replacement.getParameters()[0].replace(params[0]);

                /* replace parameters list */
                call.getParameterList().replace(replacement.getParameterList());
            }
        }
    }

}

