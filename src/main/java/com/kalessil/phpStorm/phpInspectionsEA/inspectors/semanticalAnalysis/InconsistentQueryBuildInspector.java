package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class InconsistentQueryBuildInspector extends PhpInspection {
    private static final String messagePattern = "'%s' should be used instead, so http_build_query() produces result independent from key types.";

    @NotNull
    @Override
    public String getShortName() {
        return "InconsistentQueryBuildInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Inconsistent 'http_build_query(...)' result";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("ksort")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        /* pre-condition satisfied, now check if http_build_query used in the scope */
                        final Function scope = ExpressionSemanticUtil.getScope(reference);
                        if (scope != null) {
                            for (final FunctionReference candidate : PsiTreeUtil.findChildrenOfType(scope, FunctionReference.class)) {
                                if (candidate != reference && OpenapiTypesUtil.isFunctionReference(candidate)) {
                                    final String candidateName = candidate.getName();
                                    if (candidateName != null && candidateName.equals("http_build_query")) {
                                        final PsiElement[] candidateArguments = candidate.getParameters();
                                        if (candidateArguments.length > 0 && OpenapiEquivalenceUtil.areEqual(candidateArguments[0], arguments[0])) {
                                            final String replacement = String.format("ksort(%s, SORT_STRING)", arguments[0].getText());
                                            holder.registerProblem(
                                                    reference,
                                                    String.format(ReportingUtil.wrapReportedMessage(messagePattern), replacement),
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
            return ReportingUtil.wrapReportedMessage(title);
        }

        AddSortingFlagFix(@NotNull String expression) {
            super(expression);
        }
    }
}

