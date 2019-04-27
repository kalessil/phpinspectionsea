package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class FuncNumArgsCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'func_num_args()' can be used instead.";

    @NotNull
    public String getShortName() {
        return "FuncNumArgsCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("count")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && OpenapiTypesUtil.isFunctionReference(arguments[0])) {
                        final FunctionReference innerReference = (FunctionReference) arguments[0];
                        final String innerName                 = innerReference.getName();
                        if (innerName != null && innerName.equals("func_get_args")) {
                            final String namespace = innerReference.getImmediateNamespaceName();
                            holder.registerProblem(
                                    reference,
                                    message,
                                    new UseAlternativeFix(namespace + "func_num_args()")
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseAlternativeFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the suggested alternative";

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
