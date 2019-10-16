package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayValuesMissUseInspector extends PhpInspection {
    private static final String messageGeneric  = "'array_values(...)' is not making any sense here (just use it's argument).";
    private static final String messageInArray  = "'array_values(...)' is not making any sense here (just search in it's argument).";
    private static final String messageCount    = "'array_values(...)' is not making any sense here (just count it's argument).";
    private static final String messageKeys     = "'Perhaps it was intended to use 'array_keys(...)' here.";

    @NotNull
    @Override
    public String getShortName() {
        return "ArrayValuesMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'array_values(...)' misused";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

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
                                        case "sizeof":
                                            holder.registerProblem(
                                                    reference,
                                                    ReportingUtil.wrapReportedMessage(messageCount),
                                                    new ReplaceFix(innerArguments[0].getText())
                                            );
                                            break;
                                        case "in_array":
                                            holder.registerProblem(
                                                    reference,
                                                    ReportingUtil.wrapReportedMessage(messageInArray),
                                                    new ReplaceFix(innerArguments[0].getText())
                                            );
                                            break;
                                        case "array_column":
                                        case "array_combine":
                                        case "array_values":
                                        case "implode":
                                            holder.registerProblem(
                                                    reference,
                                                    ReportingUtil.wrapReportedMessage(messageGeneric),
                                                    new ReplaceFix(innerArguments[0].getText())
                                            );
                                            break;
                                        case "str_replace":
                                        case "str_ireplace":
                                        case "preg_replace":
                                            final PsiElement[] replaceArguments = outerCall.getParameters();
                                            if (replaceArguments.length >= 3 && replaceArguments[1] == reference) {
                                                holder.registerProblem(
                                                        reference,
                                                        ReportingUtil.wrapReportedMessage(messageGeneric),
                                                        new ReplaceFix(innerArguments[0].getText())
                                                );
                                            }
                                            break;
                                        case "array_slice":
                                            final PsiElement[] sliceArguments = outerCall.getParameters();
                                            if (sliceArguments.length < 4) {
                                                holder.registerProblem(
                                                        reference,
                                                        ReportingUtil.wrapReportedMessage(messageGeneric),
                                                        new ReplaceFix(innerArguments[0].getText())
                                                );
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                        /* pattern 2: foreach(array_values(...) as ...) */
                        else if (parent instanceof ForeachStatement) {
                            final ForeachStatement foreach = (ForeachStatement) parent;
                            if (foreach.getKey() == null && !foreach.getVariables().isEmpty()) {
                                holder.registerProblem(
                                        reference,
                                        ReportingUtil.wrapReportedMessage(messageGeneric),
                                        new ReplaceFix(innerArguments[0].getText())
                                );
                            }
                        }

                        /* pattern 3: array_values(array_column(...)), array_values(array_slice(...)), array_values(array_flip(...))  */
                        if (OpenapiTypesUtil.isFunctionReference(innerArguments[0])) {
                            final FunctionReference argument = (FunctionReference) innerArguments[0];
                            final String argumentName        = argument.getName();
                            if (argumentName != null) {
                                if (argumentName.equals("array_column")) {
                                    final PsiElement[] argumentArguments = argument.getParameters();
                                    if (argumentArguments.length == 2) {
                                        holder.registerProblem(
                                                reference,
                                                ReportingUtil.wrapReportedMessage(messageGeneric),
                                                new ReplaceFix(argument.getText())
                                        );
                                    }
                                } else if (argumentName.equals("array_slice")) {
                                    final PsiElement[] argumentArguments = argument.getParameters();
                                    if (argumentArguments.length < 4) {
                                        holder.registerProblem(
                                                reference,
                                                ReportingUtil.wrapReportedMessage(messageGeneric),
                                                new ReplaceFix(argument.getText())
                                        );
                                    } else if (argumentArguments.length == 4 && PhpLanguageUtil.isFalse(argumentArguments[3])) {
                                        holder.registerProblem(
                                                reference,
                                                ReportingUtil.wrapReportedMessage(messageGeneric),
                                                new ReplaceFix(argument.getText())
                                        );
                                    }
                                } else if (argumentName.equals("array_flip")) {
                                    final PsiElement[] argumentArguments = argument.getParameters();
                                    if (argumentArguments.length == 1) {
                                        final String replacement = String.format("%sarray_keys(%s)", reference.getImmediateNamespaceName(), argumentArguments[0].getText());
                                        holder.registerProblem(
                                                reference,
                                                ReportingUtil.wrapReportedMessage(messageKeys),
                                                new ReplaceFix(replacement)
                                        );
                                    }
                                }
                            }
                        }

                        /* pattern 4: array_values([ ... ]) */
                        if (innerArguments[0] instanceof ArrayCreationExpression) {
                            final PsiElement[] children = innerArguments[0].getChildren();
                            if (children.length > 0) {
                                final boolean isTarget = Arrays.stream(children)
                                        .noneMatch(e -> e instanceof ArrayHashElement || OpenapiTypesUtil.is(e.getFirstChild(), PhpTokenTypes.opVARIADIC));
                                if (isTarget) {
                                    holder.registerProblem(
                                            reference,
                                            ReportingUtil.wrapReportedMessage(messageGeneric),
                                            new ReplaceFix(innerArguments[0].getText())
                                    );
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
