package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ArrayKeysMissUseInspector extends BasePhpInspection {
    private static final String messageArrayUnique = "'array_unique(...)' is not making any sense here (array keys are unique).";
    private static final String messageCount       = "'array_keys(...)' is not making any sense here (just count it's argument).";

    @NotNull
    public String getShortName() {
        return "ArrayKeysMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_keys")) {
                    final PsiElement[] innerArguments = reference.getParameters();
                    if (innerArguments.length == 1) {
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                final FunctionReference outerCall = (FunctionReference) grandParent;
                                final String outerCallName        = outerCall.getName();
                                if (outerCallName != null) {
                                    switch (outerCallName) {
                                        case "count":
                                            holder.registerProblem(reference, messageCount, new ReplaceFix(innerArguments[0].getText()));
                                            break;
                                        case "array_unique":
                                            holder.registerProblem(outerCall, messageArrayUnique, new ReplaceFix(reference.getText()));
                                            break;
                                        /* NOTE: "array_slice" was checked, but it changes semantics */
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class ReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Remove unnecessary calls";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
