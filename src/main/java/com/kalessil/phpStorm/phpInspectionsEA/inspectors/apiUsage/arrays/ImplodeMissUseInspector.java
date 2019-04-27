package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
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

public class ImplodeMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "Consider using '%s' instead (consumes less cpu and memory resources).";

    @NotNull
    public String getShortName() {
        return "ImplodeMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String outerFunctionName = reference.getName();
                if (outerFunctionName != null && outerFunctionName.equals("implode")) {
                    final PsiElement[] outerArguments = reference.getParameters();
                    if (outerArguments.length == 2) {
                        final PsiElement targetArgument = outerArguments[1];
                        final Set<PsiElement> values    = PossibleValuesDiscoveryUtil.discover(targetArgument);
                        if (values.size() == 1) {
                            final PsiElement candidate = values.iterator().next();
                            if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                                final FunctionReference innerCall = (FunctionReference) candidate;
                                final String innerFunctionName    = innerCall.getName();
                                if (innerFunctionName != null) {
                                    if (innerFunctionName.equals("explode")) {
                                        final PsiElement[] innerArguments = innerCall.getParameters();
                                        if (innerArguments.length == 2 && this.isExclusiveUse(targetArgument, reference)) {
                                            final String replacement = String.format(
                                                    "%sstr_replace(%s, %s, %s)",
                                                    reference.getImmediateNamespaceName(),
                                                    innerArguments[0].getText(),
                                                    outerArguments[0].getText(),
                                                    innerArguments[1].getText()
                                            );
                                            holder.registerProblem(
                                                    reference,
                                                    String.format(messagePattern, replacement),
                                                    new UseAlternativeFix(replacement)
                                            );
                                        }
                                    } else if (innerFunctionName.equals("file")) {
                                        final PsiElement[] innerArguments = innerCall.getParameters();
                                        if (innerArguments.length == 1 && this.isExclusiveUse(targetArgument, reference)) {
                                            final StringLiteralExpression literal = ExpressionSemanticUtil.resolveAsStringLiteral(outerArguments[0]);
                                            if (literal != null && literal.getContents().isEmpty()) {
                                                final String replacement = String.format(
                                                        "%sfile_get_contents(%s)",
                                                        reference.getImmediateNamespaceName(),
                                                        innerArguments[0].getText()
                                                );
                                                holder.registerProblem(
                                                        reference,
                                                        String.format(messagePattern, replacement),
                                                        new UseAlternativeFix(replacement)
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        values.clear();
                    }
                }
            }

            private boolean isExclusiveUse(@NotNull PsiElement candidate, @NotNull FunctionReference outer) {
                boolean result = candidate instanceof FunctionReference;
                if (!result) {
                    final Function scope      = ExpressionSemanticUtil.getScope(outer);
                    final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                    if (body != null) {
                        final long candidateUsages = PsiTreeUtil.findChildrenOfType(body, candidate.getClass()).stream()
                                .filter(expression -> OpenapiEquivalenceUtil.areEqual(expression, candidate))
                                .count();
                        result = candidateUsages == 2;
                    }
                }
                return result;
            }
        };
    }

    private static final class UseAlternativeFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the suggested 'implode(...)' alternative";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseAlternativeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
