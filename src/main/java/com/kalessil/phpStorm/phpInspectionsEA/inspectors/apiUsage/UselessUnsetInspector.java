package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessUnsetInspector extends BasePhpInspection {
    private static final String message = "Only local copy/reference will be unset. This unset can probably be removed.";

    @NotNull
    public String getShortName() {
        return "UselessUnsetInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {

        /* foreach is also a case, but there is no way to get flow entry point in actual JB platform API */
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                if (this.isContainingFileSkipped(method)) { return; }

                this.inspectUsages(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                if (this.isContainingFileSkipped(function)) { return; }

                this.inspectUsages(function.getParameters(), function);
            }

            private void inspectUsages(@NotNull Parameter[] parameters, @NotNull PhpScopeHolder objScopeHolder) {
                final PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                for (Parameter parameter : parameters) {
                    final String parameterName = parameter.getName();
                    if (StringUtils.isEmpty(parameterName)) {
                        continue;
                    }

                    /* find all usages of a parameter */
                    PhpAccessVariableInstruction[] usages =
                            PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, parameterName, false);
                    if (usages.length == 0) {
                        continue;
                    }

                    /* iterate over usages */
                    for (PhpAccessVariableInstruction instruction : usages) {
                        final PsiElement expression = instruction.getAnchor();
                        /* target pattern detection */
                        if (expression.getParent() instanceof PhpUnset) {
                            /* choose warning target */
                            int unsetParametersCount = ((PhpUnset) expression.getParent()).getArguments().length;
                            final PsiElement target  = (unsetParametersCount == 1 ? expression.getParent() : expression);
                            holder.registerProblem(target, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }
                    }
                }
            }
        };
    }
}