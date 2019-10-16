package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

public class SuspiciousVariableDeclarationInspector extends PhpInspection {
    private static final String messageSameParameter = "There is a parameter named '%s' already.";
    private static final String messageSameUse       = "There is a use-argument named '%s' already.";

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousVariableDeclarationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious variable declaration";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (!method.isAbstract()) {
                    this.inspect(method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                this.inspect(function);
            }

            private void inspect(@NotNull Function function) {
                final List<Variable> useVariables = ExpressionSemanticUtil.getUseListVariables(function);
                final boolean hasUseVariables     = useVariables != null && !useVariables.isEmpty();
                if (hasUseVariables) {
                    final List<Parameter> parameters  = Arrays.asList(function.getParameters());
                    final boolean hasParameters       = !parameters.isEmpty();
                    for (final Variable variable : useVariables) {
                        final String variableName = variable.getName();
                        /* pattern: use variable matches any parameter */
                        if (hasParameters && parameters.stream().anyMatch(p -> variableName.equals(p.getName()))) {
                            holder.registerProblem(
                                    variable,
                                    String.format(ReportingUtil.wrapReportedMessage(messageSameParameter), variableName)
                            );
                        }
                    }
                }
                /* release references */
                if (hasUseVariables) { useVariables.clear(); }
            }

            @Override
            public void visitPhpStaticStatement(@NotNull PhpStaticStatement staticStatement) {
                if (this.shouldSkipAnalysis(staticStatement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final Function scope = ExpressionSemanticUtil.getScope(staticStatement);
                if (scope != null) {
                    final List<Parameter> parameters  = Arrays.asList(scope.getParameters());
                    final boolean hasParameters       = !parameters.isEmpty();
                    final List<Variable> useVariables = ExpressionSemanticUtil.getUseListVariables(scope);
                    final boolean hasUseVariables     = useVariables != null && !useVariables.isEmpty();

                    staticStatement.getDeclarations().forEach(declaration -> {
                        final PhpPsiElement declared = declaration.getVariable();
                        if (declared instanceof Variable) {
                            final String variableName = declared.getName();
                            if (variableName != null && !variableName.isEmpty()) {
                                /* pattern: static variable matches a parameter */
                                if (hasParameters && parameters.stream().anyMatch(parameter -> variableName.equals(parameter.getName()))) {
                                    holder.registerProblem(
                                            declared,
                                            String.format(ReportingUtil.wrapReportedMessage(messageSameParameter), variableName)
                                    );
                                }
                                /* pattern: static variable matches a use-variable */
                                if (hasUseVariables && useVariables.stream().anyMatch(variable -> variableName.equals(variable.getName()))) {
                                    holder.registerProblem(
                                            declared,
                                            String.format(ReportingUtil.wrapReportedMessage(messageSameUse), variableName)
                                    );
                                }
                            }
                        }
                    });
                    /* release references */
                    if (hasUseVariables) { useVariables.clear(); }
                }
            }
        };
    }
}
