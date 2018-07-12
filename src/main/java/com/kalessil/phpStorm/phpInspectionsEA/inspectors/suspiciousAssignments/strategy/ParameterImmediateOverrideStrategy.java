package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
    private static final String message = "The parameter is overridden immediately (original value is lost).";

    static public void apply(@NotNull Function function, @NotNull ProblemsHolder holder) {
        /* general requirements for a function */
        final Parameter[] params  = function.getParameters();
        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
        if (null == body || 0 == params.length || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
            return;
        }

        final PhpEntryPointInstruction start = function.getControlFlow().getEntryPoint();
        for (final Parameter param : params) {
            /* overriding params by reference is totally fine */
            if (param.isPassByRef()) {
                continue;
            }

            final String parameterName          = param.getName();
            PhpAccessVariableInstruction[] uses = PhpControlFlowUtil.getFollowingVariableAccessInstructions(start, parameterName, false);
            /* at least 2 uses expected: override and any other operation */
            if (uses.length < 2) {
                continue;
            }

            /* first use should be a write directly in function body */
            final PhpPsiElement expression = uses[0].getAnchor();
            final PsiElement parent        = expression.getParent();
            if (OpenapiTypesUtil.isAssignment(parent) && expression == ((AssignmentExpression) parent).getVariable()) {
                /* the assignment must be directly in the body, no conditional/in-loop overrides are checked */
                final PsiElement grandParent = parent.getParent();
                if (null != grandParent && body != grandParent.getParent()) {
                    continue;
                }

                int nameHits = 0;

                /* count name hits, to identify if original value was considered */
                final Collection<Variable> vars = PsiTreeUtil.findChildrenOfType(parent, Variable.class);
                for (Variable variable : vars){
                    nameHits += parameterName.equals(variable.getName()) ? 1 : 0;
                    if (nameHits > 1) {
                        break;
                    }
                }
                vars.clear();

                /* okay, original value 100% lost */
                if (1 == nameHits) {
                    holder.registerProblem(expression, message);
                }
            }
        }
    }
}
