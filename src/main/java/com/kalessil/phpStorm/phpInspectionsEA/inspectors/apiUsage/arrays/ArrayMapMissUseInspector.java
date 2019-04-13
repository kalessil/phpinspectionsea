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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayMapMissUseInspector extends BasePhpInspection {
    private static final String message = "'array_map(..., array_slice(...))' would make more sense here (it also faster).";

    @NotNull
    public String getShortName() {
        return "ArrayMapMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_map")) {
                    final PsiElement[] innerArguments = reference.getParameters();
                    if (innerArguments.length == 2) {
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                final FunctionReference outerCall = (FunctionReference) grandParent;
                                final String outerCallName        = outerCall.getName();
                                if (outerCallName != null && outerCallName.equals("array_slice")) {
                                    final PsiElement[] outerArguments = outerCall.getParameters();
                                    if (outerArguments.length >= 2) {
                                        final List<String> arguments = Stream.of(outerArguments).map(PsiElement::getText).collect(Collectors.toList());
                                        arguments.set(0, innerArguments[1].getText());
                                        final String replacement = String.format(
                                                "%sarray_map(%s, %sarray_slice(%s))",
                                                reference.getImmediateNamespaceName(),
                                                innerArguments[0].getText(),
                                                outerCall.getImmediateNamespaceName(),
                                                String.join(", ", arguments)
                                        );
                                        arguments.clear();
                                        holder.registerProblem(outerCall, message, new ReplaceFix(replacement));
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
        private static final String title = "Use 'array_map(..., array_slice(...))' instead";

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
