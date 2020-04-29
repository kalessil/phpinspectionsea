package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousLoopInspector extends BasePhpInspection {
    // Inspection options.
    public boolean VERIFY_VARIABLES_OVERRIDE = true;

    private static final String messageMultipleConditions = "Please use && or || for multiple conditions. Currently no checks are performed after first positive result.";
    private static final String patternOverridesLoopVars  = "Variable '$%v%' is introduced in a outer loop and overridden here.";
    private static final String patternOverridesParameter = "Variable '$%v%' is introduced as a %t% parameter and overridden here.";

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious loop";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement statement) {
                if (VERIFY_VARIABLES_OVERRIDE) {
                    this.inspectVariables(statement);
                }
            }

            @Override
            public void visitPhpFor(@NotNull For statement) {
                if (statement.getConditionalExpressions().length > 1) {
                    holder.registerProblem(
                            statement.getFirstChild(),
                            MessagesPresentationUtil.prefixWithEa(messageMultipleConditions)
                    );
                }
                if (VERIFY_VARIABLES_OVERRIDE) {
                    this.inspectVariables(statement);
                }
            }

            private void inspectVariables(@NotNull PhpPsiElement loop) {
                final Set<String> loopVariables = this.getLoopVariables(loop);

                final Function function = ExpressionSemanticUtil.getScope(loop);
                if (null != function) {
                    final HashSet<String> parameters = new HashSet<>();
                    for (final Parameter param : function.getParameters()) {
                        parameters.add(param.getName());
                    }

                    loopVariables.forEach(variable -> {
                        if (parameters.contains(variable)) {
                            final String message = patternOverridesParameter
                                .replace("%v%", variable)
                                .replace("%t%", function instanceof Method ? "method" : "function");
                            holder.registerProblem(
                                    loop.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(message)
                            );
                        }
                    });
                    parameters.clear();
                }

                /* scan parents until reached file/callable */
                PsiElement parent = loop.getParent();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    /* inspect parent loops for conflicted variables */
                    if (parent instanceof For || parent instanceof ForeachStatement) {
                        final Set<String> parentVariables = this.getLoopVariables((PhpPsiElement) parent);
                        loopVariables.forEach(variable -> {
                            if (parentVariables.contains(variable)) {
                                holder.registerProblem(
                                        loop.getFirstChild(),
                                        MessagesPresentationUtil.prefixWithEa(patternOverridesLoopVars.replace("%v%", variable))
                                );
                            }
                        });
                        parentVariables.clear();
                    }

                    parent = parent.getParent();
                }
                loopVariables.clear();
            }

            @NotNull
            private Set<String> getLoopVariables(@NotNull PhpPsiElement loop) {
                final Set<String> variables = new HashSet<>();
                if (loop instanceof For) {
                    /* get variables from assignments */
                    Stream.of(((For) loop).getInitialExpressions()).forEach(init -> {
                        if (init instanceof AssignmentExpression) {
                            final PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                            if (variable instanceof Variable) {
                                final String variableName = variable.getName();
                                if (variableName != null) {
                                    variables.add(variableName);
                                }
                            }
                        }
                    });
                } else if (loop instanceof ForeachStatement) {
                    ((ForeachStatement) loop).getVariables().forEach(variable -> variables.add(variable.getName()));
                }

                return variables;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
                component.addCheckbox("Verify overriding scope variables", VERIFY_VARIABLES_OVERRIDE, (isSelected) -> VERIFY_VARIABLES_OVERRIDE = isSelected)
        );
    }
}