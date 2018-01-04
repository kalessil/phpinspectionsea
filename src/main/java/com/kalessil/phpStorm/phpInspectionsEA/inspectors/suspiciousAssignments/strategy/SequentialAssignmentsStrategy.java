package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
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
    private static final String patternProbableElse = "%s is immediately overridden, perhaps it was intended to use 'else' here.";
    private static final String patternGeneral      = "%s is immediately overridden, please check this code fragment.";

    static public void apply(@NotNull AssignmentExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent    = expression.getParent();
        final PsiElement container = expression.getVariable();
        if (
            container != null && OpenapiTypesUtil.isStatementImpl(parent) &&
            !isArrayPush(container) && !isContainerUsed(container, expression)
        ) {
            final PhpPsiElement previous = ((PhpPsiElement) parent).getPrevPsiSibling();
            if (previous != null) {
                if (previous instanceof If) {
                    handlePrecedingIf(container, (If) previous, holder);
                } else if (previous.getFirstChild() instanceof AssignmentExpression) {
                    handlePrecedingAssignment(container, (AssignmentExpression) previous.getFirstChild(), holder);
                }
            }
        }
    }

    /* TODO: ContextUtil::isArrayPush() - parent is assignment and [] is at any level */
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
        if (expression != null) {
            for (final PsiElement matchCandidate : PsiTreeUtil.findChildrenOfType(expression, container.getClass())) {
                if (matchCandidate != container && OpeanapiEquivalenceUtil.areEqual(matchCandidate, container)) {
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
        final PsiElement lastStatement = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
        if (lastStatement != null) {
            final boolean isReturnPoint =
                    lastStatement.getFirstChild() instanceof PhpExit ||
                    lastStatement instanceof PhpReturn   || lastStatement instanceof PhpThrow ||
                    lastStatement instanceof PhpContinue || lastStatement instanceof PhpBreak;
            if (!isReturnPoint) {
                for (final PsiElement bodyStatement : body.getChildren()) {
                    final PsiElement candidateExpression = bodyStatement.getFirstChild();
                    if (candidateExpression instanceof AssignmentExpression) {
                        final PsiElement candidate = ((AssignmentExpression) candidateExpression).getVariable();
                        if (candidate != null && OpeanapiEquivalenceUtil.areEqual(candidate, container)) {
                            final String message = String.format(patternProbableElse, container.getText());
                            holder.registerProblem(container.getParent(), message, ProblemHighlightType.GENERIC_ERROR);
                            break;
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
        if (previousContainer != null && OpeanapiEquivalenceUtil.areEqual(previousContainer, container)) {
            PsiElement operation = previousContainer.getNextSibling();
            while (operation != null && operation.getNode().getElementType() != PhpTokenTypes.opASGN) {
                operation = operation.getNextSibling();
            }
            if (operation != null) {
                /* false-positives: preceding assignments by reference */
                if (operation.getText().replaceAll("\\s+", "").equals("=&")) {
                    return;
                }
                /* false-positives: ++/-- are used inside the container expression */
                for (final UnaryExpression unary : PsiTreeUtil.findChildrenOfType(container, UnaryExpression.class)) {
                    final PsiElement unaryOperation = unary.getOperation();
                    final IElementType unaryType    = unaryOperation == null ? null : unaryOperation.getNode().getElementType();
                    if (unaryOperation != null && PhpTokenTypes.tsUNARY_POSTFIX_OPS.contains(unaryType)) {
                        return;
                    }
                }

                final String message = String.format(patternGeneral, container.getText());
                holder.registerProblem(container, message, ProblemHighlightType.GENERIC_ERROR);
            }
        }
    }
}
