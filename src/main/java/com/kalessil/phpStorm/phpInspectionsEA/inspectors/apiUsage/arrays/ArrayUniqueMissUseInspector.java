package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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

public class ArrayUniqueMissUseInspector extends BasePhpInspection {
    private static final String message = "'array_unique(...)' is not making any sense here (array keys are unique).";

    @NotNull
    public String getShortName() {
        return "ArrayUniqueMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == 1 && functionName != null && functionName.equals("array_unique")) {
                    final boolean isTarget = OpenapiTypesUtil.isFunctionReference(arguments[0]);
                    if (isTarget) {
                        final String innerName = ((FunctionReference) arguments[0]).getName();
                        if (innerName != null && innerName.equals("array_keys")) {
                            holder.registerProblem(reference, message, new ReplaceFix(arguments[0].getText()));
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
            return "Remove unnecessary 'array_unique(...)' call";
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
