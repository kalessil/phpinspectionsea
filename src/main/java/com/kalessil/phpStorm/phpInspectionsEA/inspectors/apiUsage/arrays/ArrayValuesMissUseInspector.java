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
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
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
    private static final String messageGeneric  = "'array_values(...)' is not making any sense here (just use it's argument).";
    private static final String messageInArray  = "'array_values(...)' is not making any sense here (just search in it's argument).";
    private static final String messageCount    = "'array_values(...)' is not making any sense here (just count it's argument).";

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
                        /* pattern 1: outer call doesn't need array_values */
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
                                        case "array_column":
                                        case "array_combine":
                                        case "array_values":
                                        case "implode":
                                            holder.registerProblem(reference, messageGeneric, new ReplaceFix(innerArguments[0].getText()));
                                            break;
                                        case "str_replace":
                                        case "str_ireplace":
                                        case "preg_replace":
                                            final PsiElement[] replaceArguments = outerCall.getParameters();
                                            if (replaceArguments.length >= 3 && replaceArguments[1] == reference) {
                                                holder.registerProblem(reference, messageGeneric, new ReplaceFix(innerArguments[0].getText()));
                                            }
                                            break;
                                        case "array_slice":
                                            final PsiElement[] sliceArguments = outerCall.getParameters();
                                            if (sliceArguments.length < 4) {
                                                holder.registerProblem(reference, messageGeneric, new ReplaceFix(innerArguments[0].getText()));
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                        /* pattern 2: foreach(array_values as ...) */
                        else if (parent instanceof ForeachStatement) {
                            final ForeachStatement foreach = (ForeachStatement) parent;
                            if (foreach.getKey() == null && !foreach.getVariables().isEmpty()) {
                                holder.registerProblem(reference, messageGeneric, new ReplaceFix(innerArguments[0].getText()));
                            }
                        }

                        /* pattern 3: array_values(array_column(...)), array_values(array_slice(...)),  */
                        if (OpenapiTypesUtil.isFunctionReference(innerArguments[0])) {
                            final FunctionReference argument = (FunctionReference) innerArguments[0];
                            final String argumentName        = argument.getName();
                            if (argumentName != null) {
                                if (argumentName.equals("array_column")) {
                                    final PsiElement[] argumentArguments = argument.getParameters();
                                    if (argumentArguments.length == 2) {
                                        holder.registerProblem(reference, messageGeneric, new ReplaceFix(argument.getText()));
                                    }
                                } else if (argumentName.equals("array_slice")) {
                                    final PsiElement[] argumentArguments = argument.getParameters();
                                    if (argumentArguments.length < 4) {
                                        holder.registerProblem(reference, messageGeneric, new ReplaceFix(argument.getText()));
                                    } else if (argumentArguments.length == 4 && PhpLanguageUtil.isFalse(argumentArguments[3])) {
                                        holder.registerProblem(reference, messageGeneric, new ReplaceFix(argument.getText()));
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
