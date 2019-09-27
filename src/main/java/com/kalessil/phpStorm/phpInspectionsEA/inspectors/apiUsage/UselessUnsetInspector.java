package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UselessUnsetInspector extends PhpInspection {
    private static final String message = "Only local copy/reference will be unset. This unset can probably be removed.";

    @NotNull
    @Override
    public String getShortName() {
        return "UselessUnsetInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Useless unset";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {

        /* foreach is also a case, but there is no way to get flow entry point in actual JB platform API */
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_UNUSED)) { return; }

                this.inspectUsages(method.getParameters(), method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_UNUSED)) { return; }

                this.inspectUsages(function.getParameters(), function);
            }

            private void inspectUsages(@NotNull Parameter[] parameters, @NotNull PhpScopeHolder scope) {
                if (parameters.length > 0) {
                    final PhpEntryPointInstruction entry = scope.getControlFlow().getEntryPoint();
                    for (final Parameter parameter : parameters) {
                        final String parameterName = parameter.getName();
                        if (!parameterName.isEmpty()) {
                            final PhpAccessVariableInstruction[] usages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(entry, parameterName, false);
                            for (final PhpAccessVariableInstruction usage : usages) {
                                final PsiElement expression = usage.getAnchor();
                                if (expression.getParent() instanceof PhpUnset) {
                                    int unsetParametersCount = ((PhpUnset) expression.getParent()).getArguments().length;
                                    final PsiElement target  = (unsetParametersCount == 1 ? expression.getParent() : expression);
                                    holder.registerProblem(target, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}