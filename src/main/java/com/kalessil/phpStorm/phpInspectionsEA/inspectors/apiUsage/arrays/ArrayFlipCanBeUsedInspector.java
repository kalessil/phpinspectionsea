package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ArrayFlipCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' would fit more here (it also faster).";

    @NotNull
    public String getShortName() {
        return "ArrayFlipCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_combine")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && OpenapiTypesUtil.isFunctionReference(arguments[1])) {
                        final FunctionReference innerCall = (FunctionReference) arguments[1];
                        final String innerFunctionName    = innerCall.getName();
                        if (innerFunctionName != null && innerFunctionName.equals("array_keys")) {
                            final PsiElement[] innerArguments = innerCall.getParameters();
                            if (innerArguments.length == 1 && OpenapiEquivalenceUtil.areEqual(innerArguments[0], arguments[0])) {
                                final String replacement = String.format("array_flip(%s)", arguments[0].getText());
                                holder.registerProblem(
                                        reference,
                                        String.format(messagePattern, replacement),
                                        new UseArrayFlipFixer(replacement)
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseArrayFlipFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Use array_flip(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseArrayFlipFixer(@NotNull String expression) {
            super(expression);
        }
    }
}
