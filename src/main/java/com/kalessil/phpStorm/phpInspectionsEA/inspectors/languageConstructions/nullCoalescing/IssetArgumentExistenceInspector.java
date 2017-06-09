package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IssetArgumentExistenceInspector extends BasePhpInspection {
    private static final String messagePattern = "'$%v%' seems to be not defined in the scope.";

    @NotNull
    public String getShortName() {
        return "IssetArgumentExistenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final PsiElement leftOperand = expression.getLeftOperand();
                if (PhpTokenTypes.opCOALESCE == expression.getOperationType() && leftOperand instanceof Variable) {
                    final Variable variable = (Variable) leftOperand;
                    if (!this.isSuppliedFromOutside(variable, this.getSuppliedVariables(expression))) {
                        analyzeExistence(variable);
                    }
                }
            }

            @Override
            public void visitPhpEmpty(@NotNull PhpEmpty expression) {
                this.analyzeArgumentsExistence(expression.getVariables());
            }

            @Override
            public void visitPhpIsset(@NotNull PhpIsset expression) {
                this.analyzeArgumentsExistence(expression.getVariables());
            }

            private void analyzeArgumentsExistence(@NotNull PhpExpression[] arguments) {
                final Set<String> parameters = arguments.length > 0 ? this.getSuppliedVariables(arguments[0]) : null;
                for (final PhpExpression argument : arguments) {
                    if (argument instanceof Variable) {
                        final Variable variable = (Variable) argument;
                        if (!this.isSuppliedFromOutside(variable, parameters)) {
                            analyzeExistence(variable);
                        }
                    }
                }
            }

            private void analyzeExistence (@NotNull Variable variable) {
                final Function scope      = ExpressionSemanticUtil.getScope(variable);
                final String variableName = variable.getName();
                if (scope != null && !variableName.equals("this")) {
                    final PhpAccessVariableInstruction[] usages
                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(
                            scope.getControlFlow().getEntryPoint(),
                            variableName,
                            false
                        );
                    final PhpAccessVariableInstruction firstUsage = usages.length > 0 ? usages[0] : null;
                    final PsiElement candidate                    = null == firstUsage ? null : firstUsage.getAnchor();
                    if (candidate != null) {
                        if (candidate == variable || candidate.getParent() == variable.getParent()) {
                            final String message = messagePattern.replace("%v%", variableName);
                            holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }

            @Nullable
            private Set<String> getSuppliedVariables(@NotNull PsiElement expression) {
                final Function scope  = ExpressionSemanticUtil.getScope(expression);
                Set<String> variables = null;
                if (scope != null) {
                    variables = new HashSet<>();
                    for (final Parameter parameter : scope.getParameters()) {
                        variables.add(parameter.getName());
                    }
                    final List<Variable> usedVariables = ExpressionSemanticUtil.getUseListVariables(scope);
                    if (usedVariables != null) {
                        for (final Variable usedVariable : usedVariables) {
                            variables.add(usedVariable.getName());
                        }
                    }
                }
                return variables;
            }

            private boolean isSuppliedFromOutside(@NotNull Variable variable, @Nullable Set<String> suppliedVariables) {
                return suppliedVariables != null && suppliedVariables.contains(variable.getName());
            }
        };
    }
}
