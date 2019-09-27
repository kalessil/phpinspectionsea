package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ImplodeMissUseInspector extends PhpInspection {
    private static final String messagePattern    = "Consider using '%s' instead (consumes less cpu and memory resources).";
    private static final String messageSprintf    = "Consider taking advantage of the outer 'sprintf(...)' call instead (simplification).";
    private static final String messageBuildQuery = "Consider taking advantage of using 'http_build_query(...)' here (simplification).";

    @NotNull
    @Override
    public String getShortName() {
        return "ImplodeMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'implode(...)' misused";
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
                        final PsiElement arrayArgument = outerArguments[1];
                        if (arrayArgument instanceof ArrayCreationExpression) {
                            /* case: sprintf argument */
                            final PsiElement parent  = reference.getParent();
                            final PsiElement context = parent instanceof ParameterList ? parent.getParent() : parent;
                            if (OpenapiTypesUtil.isFunctionReference(context)) {
                                final String wrappingFunctionName = ((FunctionReference) context).getName();
                                if (wrappingFunctionName != null && wrappingFunctionName.equals("sprintf")) {
                                    final boolean isTarget = Arrays.stream(arrayArgument.getChildren())
                                            .noneMatch(e -> e instanceof ArrayHashElement || OpenapiTypesUtil.is(e.getFirstChild(), PhpTokenTypes.opVARIADIC));
                                    if (isTarget) {
                                        holder.registerProblem(reference, messageSprintf);
                                    }
                                }
                            }
                            /* case: just one element */
                            final PsiElement[] values = arrayArgument.getChildren();
                            if (values.length == 1 && ! (values[0] instanceof ArrayHashElement)) {
                                final boolean isTarget = !OpenapiTypesUtil.is(values[0].getFirstChild(), PhpTokenTypes.opVARIADIC);
                                if (isTarget) {
                                    holder.registerProblem(reference, String.format(messagePattern, values[0].getText()));
                                }
                            }
                        } else {
                            final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arrayArgument);
                            if (values.size() == 1) {
                                final PsiElement candidate = values.iterator().next();
                                if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                                    final FunctionReference innerCall = (FunctionReference) candidate;
                                    final String innerFunctionName    = innerCall.getName();
                                    if (innerFunctionName != null) {
                                        if (innerFunctionName.equals("explode")) {
                                            final PsiElement[] innerArguments = innerCall.getParameters();
                                            if (innerArguments.length == 2 && this.isExclusiveUse(arrayArgument, reference)) {
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
                                            if (innerArguments.length == 1 && this.isExclusiveUse(arrayArgument, reference)) {
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

                        final PsiElement glueArgument = outerArguments[0];
                        if (glueArgument instanceof StringLiteralExpression) {
                            /* case: mimics http_build_query('(&amp;)|&') behaviour */
                            final String glue = ((StringLiteralExpression) glueArgument).getContents();
                            if (glue.equals("&") || glue.equals("&amp;")) {
                                holder.registerProblem(reference, messageBuildQuery);
                            }
                        }
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
