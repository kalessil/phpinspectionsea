package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
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

    private static final Set<String> magicVariables = new HashSet<>();
    static {
        magicVariables.add("this");
        magicVariables.add("php_errormsg");
    }

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
                if (leftOperand instanceof Variable && PhpTokenTypes.opCOALESCE == expression.getOperationType()) {
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
                final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                final String variableName = variable.getName();
                if (body != null && !magicVariables.contains(variableName) && !ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                    for (final Variable reference : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                        if (reference.getName().equals(variableName)) {
                            boolean report = reference == variable;
                            if (!report && reference.getParent() instanceof AssignmentExpression) {
                                report = PsiTreeUtil.findCommonParent(reference, variable) == reference.getParent();
                            }
                            if (report) {
                                final String message = messagePattern.replace("%v%", variableName);
                                holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);
                            }
                            break;
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
