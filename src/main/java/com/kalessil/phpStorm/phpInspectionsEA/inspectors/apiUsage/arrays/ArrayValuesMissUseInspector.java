package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
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

public class ArrayValuesMissUseInspector extends BasePhpInspection {
    private static final String messageForeach       = "'array_values(...)' is not making any sense here (just use it's argument).";
    private static final String messageStringReplace = "'array_values(...)' is not making any sense here (just use it's argument).";
    private static final String messageInArray       = "'array_values(...)' is not making any sense here (just search in it's argument).";
    private static final String messageCount         = "'array_values(...)' is not making any sense here (just count it's argument).";
    private static final String messageArraySlice    = "'%s' is making more sense here (reduces amount of processed elements).";

    @NotNull
    public String getShortName() {
        return "ArrayValuesMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_values")) {
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
                                        case "in_array":
                                            holder.registerProblem(reference, messageInArray, new ReplaceFix(innerArguments[0].getText()));
                                            break;
                                        case "str_replace":
                                            final PsiElement[] replaceArguments = outerCall.getParameters();
                                            if (replaceArguments.length == 3 && replaceArguments[1] == reference) {
                                                holder.registerProblem(reference, messageStringReplace, new ReplaceFix(innerArguments[0].getText()));
                                            }
                                            break;
                                        case "array_slice":
                                            final PsiElement[] sliceArguments = outerCall.getParameters();
                                            final String theArray             = innerArguments[0].getText();
                                            final String newInnerCall         = outerCall.getText().replace(sliceArguments[0].getText(), theArray);
                                            final String replacement          = reference.getText().replace(theArray, newInnerCall);
                                            final String message              = String.format(messageArraySlice, replacement);
                                            holder.registerProblem(outerCall, message, new ReplaceFix(replacement));
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        } else if (parent instanceof ForeachStatement) {
                            final ForeachStatement foreach = (ForeachStatement) parent;
                            if (foreach.getKey() == null && !foreach.getVariables().isEmpty()) {
                                holder.registerProblem(reference, messageForeach, new ReplaceFix(innerArguments[0].getText()));
                            }
                        }
                    }
                }
            }
        };
    }

    private class ReplaceFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Remove unnecessary calls";
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
