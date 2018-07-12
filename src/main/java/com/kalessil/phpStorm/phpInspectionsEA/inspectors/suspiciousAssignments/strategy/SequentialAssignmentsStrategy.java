package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SequentialAssignmentsStrategy {
    private static final String patternConditional = "%s is immediately overridden, perhaps it was intended to use 'else' here.";
    private static final String patternGeneral     = "%s is immediately overridden, please check this code fragment.";

    static public void apply(@NotNull AssignmentExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent    = expression.getParent();
        final PsiElement container = expression.getVariable();
        if (container != null && OpenapiTypesUtil.isStatementImpl(parent)) {
            final boolean isTargetExpression = !isValidArrayWrite(container) && !isContainerUsed(container, expression);
            if (isTargetExpression) {
                final PhpPsiElement previous = ((PhpPsiElement) parent).getPrevPsiSibling();
                if (previous != null) {
                    if (previous instanceof If) {
                        handlePrecedingIf(container, (If) previous, holder);
                    } else {
                        final PsiElement candidate = previous.getFirstChild();
                        if (OpenapiTypesUtil.isAssignment(candidate)) {
                            handlePrecedingAssignment(container, (AssignmentExpression) candidate, holder);
                        }
                    }
                }
            }
        }
    }

    static private boolean isValidArrayWrite(@NotNull PsiElement container) {
        boolean result = false;
        while (container instanceof ArrayAccessExpression) {
            final ArrayAccessExpression expression = (ArrayAccessExpression) container;
            final ArrayIndex index                 = expression.getIndex();
            if (index != null) {
                final PsiElement indexValue = index.getValue();
                if (result = (indexValue == null)) {
                    break;
                } else if (indexValue instanceof ConstantReference) {
                    final String name = ((ConstantReference) indexValue).getName();
                    if (result = (name != null && name.equals("__LINE__"))) {
                        break;
                    }
                }
            }
            container = expression.getValue();
        }
        return result;
    }

    static private boolean isContainerUsed(@NotNull PsiElement container, @Nullable AssignmentExpression assignment) {
        boolean result = false;
        if (assignment != null) {
            /* check if container is used */
            for (final PsiElement candidate : PsiTreeUtil.findChildrenOfType(assignment.getValue(), container.getClass())) {
                if (OpenapiEquivalenceUtil.areEqual(candidate, container)) {
                    result = true;
                    break;
                }
            }
            /* check if container expression part are interconnected */
            if (!result) {
                final Set<String> variables = new HashSet<>();
                for (final Variable variable : PsiTreeUtil.findChildrenOfType(container, Variable.class)) {
                    final String variableName = variable.getName();
                    if (!variableName.equals("this") && !variables.add(variableName)) {
                        result = true;
                        break;
                    }
                }
                variables.clear();
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
                    lastStatement instanceof PhpReturn   || lastStatement instanceof PhpThrow ||
                    lastStatement instanceof PhpContinue || lastStatement instanceof PhpBreak ||
                    lastStatement.getFirstChild() instanceof PhpExit;
            if (!isReturnPoint) {
                PhpPsiElement found = null;
                /* identify conditional assignment */
                for (final PsiElement bodyStatement : body.getChildren()) {
                    final PsiElement candidateExpression = bodyStatement.getFirstChild();
                    if (candidateExpression instanceof AssignmentExpression) {
                        final PsiElement candidate = ((AssignmentExpression) candidateExpression).getVariable();
                        if (candidate != null && OpenapiEquivalenceUtil.areEqual(candidate, container)) {
                            found = (PhpPsiElement) bodyStatement;
                            break;
                        }
                    }
                }
                /* check read context and do reporting */
                if (found != null) {
                    final PsiElement next              = found.getNextPsiSibling();
                    final PsiElement consumerCandidate = next instanceof If ? ((If) next).getCondition() : next;
                    final boolean isUsed               = consumerCandidate != null &&
                            PsiTreeUtil.findChildrenOfType(consumerCandidate, container.getClass()).stream()
                                    .anyMatch(candidate -> OpenapiEquivalenceUtil.areEqual(candidate, container));
                    if (!isUsed) {
                        final String message = String.format(patternConditional, container.getText());
                        holder.registerProblem(container.getParent(), message);
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
        if (previousContainer != null && OpenapiEquivalenceUtil.areEqual(previousContainer, container)) {
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
                holder.registerProblem(container, message);
            }
        }
    }
}
