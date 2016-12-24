package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ParameterImmediateOverrideStrategy {
    private static final String message = "Parameter is overridden immediately (original value lost completely)";

    static public void apply(@NotNull final Function function, @NotNull final ProblemsHolder holder) {
        /* general requirements for a function */
        final Parameter[] params  = function.getParameters();
        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
        if (0 == params.length || null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
            return;
        }

        final PhpEntryPointInstruction start = function.getControlFlow().getEntryPoint();
        for (Parameter param : params) {
            final String parameterName                = param.getName();
            final PhpAccessVariableInstruction[] uses =
                        PhpControlFlowUtil.getFollowingVariableAccessInstructions(start, parameterName, false);
            /* at least 2 uses expected: override and any other operation */
            if (uses.length < 2) {
                continue;
            }

            /* first use should be a write */
            final PhpPsiElement expression = uses[0].getAnchor();
            final PsiElement parent        = expression.getParent();
            if (parent instanceof AssignmentExpression && expression == ((AssignmentExpression) parent).getVariable()) {
                int nameHits = 0;

                /* count name hits, to identify if original value was considered */
                Collection<Variable> vars = PsiTreeUtil.findChildrenOfType(parent, Variable.class);
                for (Variable variable : vars){
                    nameHits += parameterName.equals(variable.getName()) ? 1 : 0;
                    if (nameHits >= 2) {
                        break;
                    }
                }
                vars.clear();

                /* okay, original value 100% lost */
                if (1 == nameHits) {
                    holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
    }
}
