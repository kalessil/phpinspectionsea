package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

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

public class NullCoalescingArgumentExistenceInspector extends BasePhpInspection {
    private static final String messagePattern = "'$%v%' seems to be not defined in the scope.";

    @NotNull
    public String getShortName() {
        return "NullCoalescingArgumentExistenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                final PhpLanguageLevel phpVersion
                        = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    return;
                }

                final PsiElement leftOperand = expression.getLeftOperand();
                if (PhpTokenTypes.opCOALESCE == expression.getOperationType() && leftOperand instanceof Variable) {
                    final Variable variable = (Variable) leftOperand;
                    if (!this.isSuppliedFromOutside(variable)) {
                        analyzeExistence(variable);
                    }
                }
            }

            private void analyzeExistence (@NotNull Variable variable) {
                final Function scope = ExpressionSemanticUtil.getScope(variable);
                if (null != scope) {
                    final String variableName = variable.getName();
                    final PhpAccessVariableInstruction[] usages
                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(
                            scope.getControlFlow().getEntryPoint(),
                            variableName,
                            false
                        );
                    final PhpAccessVariableInstruction firstUsage = usages.length > 0 ? usages[0] : null;
                    final PsiElement candidate                    = null == firstUsage ? null : firstUsage.getAnchor();
                    if (null != candidate) {
                        if (candidate == variable || candidate.getParent() == variable.getParent()) {
                            final String message = messagePattern.replace("%v%", variableName);
                            holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }

            private boolean isSuppliedFromOutside(@NotNull Variable variable) {
                final Set<String> suppliedVariables = new HashSet<>();
                final Function scope                = ExpressionSemanticUtil.getScope(variable);
                if (null != scope) {
                    for (final Parameter parameter : scope.getParameters()) {
                        suppliedVariables.add(parameter.getName());
                    }
                    final List<Variable> usedVariables = ExpressionSemanticUtil.getUseListVariables(scope);
                    if (null != usedVariables) {
                        for (final Variable usedVariable : usedVariables) {
                            suppliedVariables.add(usedVariable.getName());
                        }
                    }
                }

                return suppliedVariables.contains(variable.getName());
            }
        };
    }
}
