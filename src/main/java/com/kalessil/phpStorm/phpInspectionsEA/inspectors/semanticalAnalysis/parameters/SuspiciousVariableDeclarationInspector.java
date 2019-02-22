package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousVariableDeclarationInspector extends BasePhpInspection {
    private static final String messageSameParameter = "There is a parameter named '%s' already.";
    private static final String messageSameUse       = "There is a use-argument named '%s' already.";

    @NotNull
    public String getShortName() {
        return "CallableParameterUseCaseInTypeContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(method))                 { return; }

                if (!method.isAbstract()) {
                    this.inspect(method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(function))               { return; }

                this.inspect(function);
            }

            private void inspect(@NotNull Function function) {
                final List<Parameter> parameters  = Arrays.asList(function.getParameters());

                /* pattern: use variable matches any parameter */
                final List<Variable> useVariables = ExpressionSemanticUtil.getUseListVariables(function);
                if (useVariables != null && !useVariables.isEmpty()) {
                    for (final Variable variable : useVariables) {
                        final String variableName = variable.getName();
                        if (parameters.stream().anyMatch(p -> variableName.equals(p.getName()))) {
                            holder.registerProblem(variable, String.format(messageSameParameter, variableName));
                        }
                    }
                }

                /* pattern: static variables matches any parameter or use variables */
            }
        };
    }
}
