package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiControlFlowUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ParameterImmediateOverrideStrategy {
    private static final String message = "This variable name has already been declared previously without being used.";

    static public void apply(@NotNull Function function, @NotNull ProblemsHolder holder) {
        /* general requirements for a function */
        final Parameter[] params  = function.getParameters();
        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
        if (body == null || params.length == 0 || ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
            return;
        }

        final PhpEntryPointInstruction start = function.getControlFlow().getEntryPoint();
        for (final Parameter param : params) {
            /* overriding params by reference is totally fine */
            if (param.isPassByRef()) {
                continue;
            }

            final String parameterName              = param.getName();
            List<PhpAccessVariableInstruction> uses = OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(start, parameterName);
            /* at least 2 uses expected: override and any other operation */
            if (uses.size() < 2) {
                continue;
            }

            /* first use should be a write directly in function body */
            final PhpPsiElement expression = uses.get(0).getAnchor();
            final PsiElement parent        = expression.getParent();
            if (OpenapiTypesUtil.isAssignment(parent) && expression == ((AssignmentExpression) parent).getVariable()) {
                /* the assignment must be directly in the body, no conditional/in-loop overrides are checked */
                final PsiElement grandParent = parent.getParent();
                if (grandParent != null && body != grandParent.getParent()) {
                    continue;
                }

                /* count name hits, to identify if original value was considered */
                int nameHits = 0;
                for (final Variable variable : PsiTreeUtil.findChildrenOfType(parent, Variable.class)) {
                    if (parameterName.equals(variable.getName()) && ++nameHits > 1) {
                        break;
                    }
                }
                if (nameHits == 1) {
                    holder.registerProblem(
                            expression,
                            MessagesPresentationUtil.prefixWithEa(message)
                    );
                }
            }
        }
    }
}
