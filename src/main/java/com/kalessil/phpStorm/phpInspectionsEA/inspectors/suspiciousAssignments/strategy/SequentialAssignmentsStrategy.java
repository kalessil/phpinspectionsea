package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SequentialAssignmentsStrategy {
    private static final String patternProbableElse = "%c% is immediately overridden, perhaps it was intended to use 'else' here.";
    private static final String patternGeneral      = "%c% is immediately overridden, please check this code fragment.";

    static public void apply(@NotNull AssignmentExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent    = expression.getParent();
        final PsiElement container = expression.getVariable();
        if (
            null != container && OpenapiTypesUtil.isStatementImpl(parent) &&
            !isArrayPush(container) && !isContainerUsed(container, expression)
        ) {
            final PhpPsiElement previous = ((PhpPsiElement) parent).getPrevPsiSibling();
            if (null != previous) {
                if (previous instanceof If) {
                    handlePrecedingIf(container, (If) previous, holder);
                } else if (previous.getFirstChild() instanceof AssignmentExpression) {
                    handlePrecedingAssignment(container, (AssignmentExpression) previous.getFirstChild(), holder);
                }
            }
        }
    }

    static private boolean isArrayPush(@NotNull PsiElement container) {
        boolean result = false;
        while (container instanceof ArrayAccessExpression) {
            final ArrayAccessExpression expression = (ArrayAccessExpression) container;
            final ArrayIndex index                 = expression.getIndex();
            if (index != null && index.getValue() == null) {
                result = true;
                break;
            }
            container = expression.getValue();
        }
        return result;
    }

    static private boolean isContainerUsed(@NotNull PsiElement container, @Nullable PsiElement expression) {
        boolean result = false;
        if (null != expression) {
            for (PsiElement matchCandidate : PsiTreeUtil.findChildrenOfType(expression, container.getClass())) {
                if (matchCandidate != container && PsiEquivalenceUtil.areElementsEquivalent(matchCandidate, container)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    static private void handlePrecedingIf(
        @NotNull PsiElement container,
        @NotNull If previous,
        @NotNull ProblemsHolder holder
    ) {
        final boolean hasOtherBranches = ExpressionSemanticUtil.hasAlternativeBranches(previous);
        final GroupStatement body      = hasOtherBranches ? null : ExpressionSemanticUtil.getGroupStatement(previous);
        final PsiElement lastStatement = null == body ? null : ExpressionSemanticUtil.getLastStatement(body);
        if (null != lastStatement) {
            final boolean isExitStatement = lastStatement.getFirstChild() instanceof PhpExit;
            final boolean isReturnPoint   = isExitStatement ||
                       lastStatement instanceof PhpReturn   || lastStatement instanceof PhpThrow ||
                       lastStatement instanceof PhpContinue || lastStatement instanceof PhpBreak;
            if (!isReturnPoint) {
                for (PsiElement bodyStatement : body.getChildren()) {
                    final PsiElement candidateExpression = bodyStatement.getFirstChild();
                    if (candidateExpression instanceof AssignmentExpression) {
                        final PsiElement candidate = ((AssignmentExpression) candidateExpression).getVariable();
                        if (null != candidate && PsiEquivalenceUtil.areElementsEquivalent(candidate, container)) {
                            final String message = patternProbableElse.replace("%c%", container.getText());
                            holder.registerProblem(container.getParent(), message, ProblemHighlightType.GENERIC_ERROR);

                            return;
                        }
                    }
                }
            }
        }
    }

    static private void handlePrecedingAssignment(
        @NotNull PsiElement container,
        @NotNull AssignmentExpression previous,
        @NotNull ProblemsHolder holder
    ) {
        final PsiElement previousContainer = previous.getVariable();
        if (previousContainer != null && PsiEquivalenceUtil.areElementsEquivalent(previousContainer, container)) {
            PsiElement operation = previousContainer.getNextSibling();
            while (operation != null && operation.getNode().getElementType() != PhpTokenTypes.opASGN) {
                operation = operation.getNextSibling();
            }
            /* preceding assignments by reference are totally making sense */
            if (operation != null && !operation.getText().replaceAll("\\s+", "").equals("=&")) {
                final String message = patternGeneral.replace("%c%", container.getText());
                holder.registerProblem(container, message, ProblemHighlightType.GENERIC_ERROR);
            }
        }
    }
}
