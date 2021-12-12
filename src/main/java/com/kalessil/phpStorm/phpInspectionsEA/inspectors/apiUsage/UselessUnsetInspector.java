package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiControlFlowUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UselessUnsetInspector extends BasePhpInspection {
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
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectUsages(method.getParameters(), method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectUsages(function.getParameters(), function);
            }

            private void inspectUsages(@NotNull Parameter[] parameters, @NotNull PhpScopeHolder scope) {
                if (parameters.length > 0) {
                    final PhpEntryPointInstruction entry = scope.getControlFlow().getEntryPoint();
                    for (final Parameter parameter : parameters) {
                        final String parameterName = parameter.getName();
                        if (!parameterName.isEmpty()) {
                            for (final PhpAccessVariableInstruction usage : OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(entry, parameterName)) {
                                final PsiElement expression = usage.getAnchor();
                                final PsiElement parent     = expression.getParent();
                                if (parent instanceof PhpUnset) {
                                    int unsetParametersCount = ((PhpUnset) parent).getArguments().length;
                                    final PsiElement target  = (unsetParametersCount == 1 ? parent : expression);
                                    holder.registerProblem(
                                            target,
                                            MessagesPresentationUtil.prefixWithEa(message),
                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}