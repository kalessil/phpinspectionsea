package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SwitchFallThroughStrategy {
    private static final String message = "Overrides value from a preceding case (perhaps a 'break' is missing there).";

    static public void apply(@NotNull PhpSwitch switchStatement, @NotNull ProblemsHolder holder) {
        final List<PsiElement> written = new ArrayList<>();
        for (PhpCase oneCase : switchStatement.getAllCases()) {
            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(oneCase);
            if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                continue;
            }

            final List<PsiElement> writtenLocally = new ArrayList<>();
            for (PsiElement expression : body.getChildren()) {
                /* get expression from '...;' constructs */
                if (expression instanceof Statement) {
                    expression = expression.getFirstChild();
                }

                if (expression instanceof MultiassignmentExpression) {
                    for (PsiElement variable : ((MultiassignmentExpression) expression).getVariables()) {
                        /* HashSet is not working here, hence manual checks */
                        boolean isOverridden = false;
                        for (final PsiElement writtenVariable : written) {
                            if (OpenapiEquivalenceUtil.areEqual(writtenVariable, variable)) {
                                isOverridden = true;
                                holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);

                                break;
                            }
                        }
                        if (!isOverridden) {
                            writtenLocally.add(variable);
                        }
                    }
                    continue;
                }

                if (OpenapiTypesUtil.isAssignment(expression)) {
                    final PsiElement variable = ((AssignmentExpression) expression).getVariable();
                    if (null != variable) {
                        /* do not process []= operations */
                        if (variable instanceof ArrayAccessExpression) {
                            final ArrayIndex index = ((ArrayAccessExpression) variable).getIndex();
                            if (null == index || null == index.getValue()) {
                                continue;
                            }
                        }

                        /* HashSet is not working here, hence manual checks */
                        boolean isOverridden = false;
                        for (final PsiElement writtenVariable : written) {
                            if (OpenapiEquivalenceUtil.areEqual(writtenVariable, variable)) {
                                isOverridden = true;
                                holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR);

                                break;
                            }
                        }
                        if (!isOverridden) {
                            writtenLocally.add(variable);
                        }
                    }
                    // continue;
                }
            }


            /* now flush local writes into shared one; manually as HashSet is not working */
            for (PsiElement localVariable : writtenLocally) {
                boolean isAddedAlready = false;
                for (final PsiElement sharedVariable : written) {
                    if (OpenapiEquivalenceUtil.areEqual(localVariable, sharedVariable)) {
                        isAddedAlready = true;
                        break;
                    }
                }
                if (!isAddedAlready) {
                    written.add(localVariable);
                }
            }
            writtenLocally.clear();


            /* once break/return met, clean the written expressions out */
            final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
            if (
                last instanceof PhpBreak ||
                last instanceof PhpReturn ||
                last instanceof PhpContinue ||
                last instanceof PhpThrow ||
                (last instanceof Statement && last.getFirstChild() instanceof PhpExit) ||
                last instanceof PhpGoto
            ) {
                written.clear();
                // continue;
            }
        }
        written.clear();
    }
}
