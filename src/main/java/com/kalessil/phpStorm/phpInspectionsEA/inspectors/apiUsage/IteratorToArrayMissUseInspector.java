package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
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

public class IteratorToArrayMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "Consider using '%s' instead (consumes less cpu and memory resources).";

    @NotNull
    public String getShortName() {
        return "IteratorToArrayMissUseInspection";
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
                if (functionName != null && functionName.equals("iterator_to_array")) {
                    final PsiElement[] arguments = reference.getParameters();
                    final PsiElement parent      = reference.getParent();
                    if (arguments.length > 0 && parent instanceof ArrayAccessExpression) {
                        final ArrayIndex indexWrapper = ((ArrayAccessExpression) parent).getIndex();
                        if (indexWrapper != null) {
                            final PsiElement index = indexWrapper.getValue();
                            if (index != null) {
                                final boolean isTarget = OpenapiTypesUtil.isNumber(index) && index.getText().equals("0");
                                if (isTarget) {
                                    final String replacement = String.format("%s->current()", arguments[0].getText());
                                    holder.registerProblem(
                                            parent,
                                            String.format(messagePattern, replacement),
                                            new UseCurrentMethodFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseCurrentMethodFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use '...->current()' instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseCurrentMethodFix(@NotNull String expression) {
            super(expression);
        }
    }
}
