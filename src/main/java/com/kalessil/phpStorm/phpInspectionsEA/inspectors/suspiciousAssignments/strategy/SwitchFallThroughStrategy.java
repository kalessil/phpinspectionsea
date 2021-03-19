package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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
        for (final PhpCase oneCase : switchStatement.getAllCases()) {
            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(oneCase);
            if (body == null || ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                continue;
            }

            final List<PsiElement> writtenLocally = new ArrayList<>();
            for (PsiElement expression : body.getChildren()) {
                /* get expression from '...;' constructs */
                if (expression instanceof Statement) {
                    expression = expression.getFirstChild();
                }

                if (expression instanceof MultiassignmentExpression) {
                    for (final PsiElement variable : ((MultiassignmentExpression) expression).getVariables()) {
                        /* HashSet is not working here, hence manual checks */
                        boolean isOverridden = false;
                        for (final PsiElement writtenVariable : written) {
                            if (OpenapiEquivalenceUtil.areEqual(writtenVariable, variable)) {
                                isOverridden = true;
                                holder.registerProblem(
                                        variable,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
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
                    final AssignmentExpression assignment = (AssignmentExpression) expression;
                    final PsiElement variable             = assignment.getVariable();
                    if (variable != null) {
                        /* false-positives: `... []= ...` */
                        if (variable instanceof ArrayAccessExpression) {
                            final ArrayIndex index = ((ArrayAccessExpression) variable).getIndex();
                            if (index == null || index.getValue() == null) {
                                continue;
                            }
                        }
                        /* false-positives: $variable = ... $variable ... */
                        final PsiElement value = assignment.getValue();
                        if (value != null) {
                            final boolean isSelfDependent = PsiTreeUtil.findChildrenOfType(value, variable.getClass()).stream()
                                    .anyMatch(v -> OpenapiEquivalenceUtil.areEqual(v, variable));
                            if (isSelfDependent) {
                                continue;
                            }
                        }

                        /* HashSet is not working here, hence manual checks */
                        boolean isOverridden = false;
                        for (final PsiElement writtenVariable : written) {
                            if (OpenapiEquivalenceUtil.areEqual(writtenVariable, variable)) {
                                isOverridden = true;
                                holder.registerProblem(
                                        variable,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
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
            for (final PsiElement localVariable : writtenLocally) {
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
                last instanceof PhpBreak    ||
                last instanceof PhpReturn   ||
                last instanceof PhpContinue ||
                last instanceof PhpGoto     ||
                (last instanceof Statement && last.getFirstChild() instanceof PhpExit) ||
                OpenapiTypesUtil.isThrowExpression(last)
            ) {
                written.clear();
                // continue;
            }
        }
        written.clear();
    }
}
