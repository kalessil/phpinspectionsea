package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class ArrayUniqueMissUseInspector extends PhpInspection {
    private static final String message = "'array_unique(array_filter(...))' would fit more here (it also slightly faster).";

    @NotNull
    @Override
    public String getShortName() {
        return "ArrayUniqueMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'array_unique(...)' misused";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_unique")) {
                    final PsiElement parent = reference.getParent();
                    final PsiElement context = parent instanceof ParameterList ? parent.getParent() : parent;
                    if (OpenapiTypesUtil.isFunctionReference(context)) {
                        final FunctionReference outerCall = (FunctionReference) context;
                        final String outerFunctionName    = outerCall.getName();
                        if (outerFunctionName != null && outerFunctionName.equals("array_filter")) {
                            final PsiElement[] arguments      = reference.getParameters();
                            final PsiElement[] outerArguments = outerCall.getParameters();
                            if (outerArguments.length == 1 && arguments.length == 1) {
                                final String replacement = String.format(
                                        "%sarray_unique(%sarray_filter(%s))",
                                        reference.getImmediateNamespaceName(),
                                        outerCall.getImmediateNamespaceName(),
                                        arguments[0].getText()
                                );
                                holder.registerProblem(
                                        outerCall,
                                        MessagesPresentationUtil.prefixWithEa(message),
                                        new ReplaceFix(replacement)
                                );
                            }
                        }
                    }
                }
            }
        };
    }


    private static final class ReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use array_unique(...) instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
