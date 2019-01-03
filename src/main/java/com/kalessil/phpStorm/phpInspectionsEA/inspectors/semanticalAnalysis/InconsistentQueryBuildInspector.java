package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
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
    private static final String messagePattern = "'%s' should be used instead, so http_build_query() produces result independent from key types.";

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
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String function = reference.getName();
                if (function != null && function.equals("ksort")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        /* pre-condition satisfied, now check if http_build_query used in the scope */
                        final Function scope = ExpressionSemanticUtil.getScope(reference);
                        if (scope != null) {
                            for (final FunctionReference call : PsiTreeUtil.findChildrenOfType(scope, FunctionReference.class)) {
                                if (call != reference && OpenapiTypesUtil.isFunctionReference(call)) {
                                    final String callFunctionName = call.getName();
                                    if (callFunctionName != null && callFunctionName.equals("http_build_query")) {
                                        final PsiElement[] callArguments = call.getParameters();
                                        if (callArguments.length > 0 && OpenapiEquivalenceUtil.areEqual(callArguments[0], arguments[0])) {
                                            final String replacement = String.format("ksort(%s, SORT_STRING)", arguments[0].getText());
                                            holder.registerProblem(
                                                    reference,
                                                    String.format(messagePattern, replacement),
                                                    new AddSortingFlagFix(replacement)
                                            );
                                            return;
                                        }
                                   }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class AddSortingFlagFix extends UseSuggestedReplacementFixer {
        private static final String title = "Add SORT_STRING as an argument";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        AddSortingFlagFix(@NotNull String expression) {
            super(expression);
        }
    }
}

