package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class DuplicateConditionsInSingleBinaryStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true when reached (the operation is incorrect or can be simplified).";
    private static final String messageAlwaysFalse = "'%s' seems to be always false when reached (the operation is incorrect or can be simplified).";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if ((operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) && isTargetContext(expression, operator)) {
            final List<PsiElement> conditions = conditions(expression, operator);
            if (! conditions.isEmpty()) {
                final int conditionsCount = conditions.size();
                if (conditionsCount > 1) {
                    for (int outerIndex = 0, outerLimit = conditionsCount - 1; outerIndex < outerLimit; ++outerIndex) {
                        final PsiElement outerElement = conditions.get(outerIndex);
                        for (int innerIndex = outerIndex + 1; innerIndex < conditionsCount; ++innerIndex) {
                            final PsiElement innerElement = conditions.get(innerIndex);
                            if (OpenapiEquivalenceUtil.areEqual(outerElement, innerElement) && ! isDirectoryExistenceCheck(innerElement, operator)) {
                                final String messageTemplate = operator == PhpTokenTypes.opAND ? messageAlwaysTrue : messageAlwaysFalse;
                                holder.registerProblem(
                                        innerElement,
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageTemplate, innerElement.getText()))
                                );
                                result = true;
                                break;
                            }
                        }
                    }
                }
                conditions.clear();
            }
        }
        return result;
    }

    private static boolean isDirectoryExistenceCheck(@NotNull PsiElement expression, @NotNull IElementType operator) {
        PsiElement subject = expression;
        if (operator == PhpTokenTypes.opAND && subject instanceof UnaryExpression) {
            final UnaryExpression unary = (UnaryExpression) subject;
            if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                subject = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
            }
        }
        return OpenapiTypesUtil.isFunctionReference(subject) && "is_dir".equals(((FunctionReference) subject).getName());
    }

    private static boolean isTargetContext(@NotNull BinaryExpression expression, @NotNull IElementType operator) {
        final PsiElement parent  = expression.getParent();
        final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
        if (context instanceof BinaryExpression) {
            return ((BinaryExpression) context).getOperationType() != operator;
        }
        return true;
    }

    @NotNull
    private static List<PsiElement> conditions(@NotNull BinaryExpression expression, @NotNull IElementType operator) {
        final ArrayList<PsiElement> result = new ArrayList<>();
        final PsiElement left              = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
        final PsiElement right             = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
        if (left != null && right != null) {
            Stream.of(left, right).forEach(element -> {
                if (element instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) element;
                    if (binary.getOperationType() == operator) {
                        result.addAll(conditions(binary, operator));
                        return;
                    }
                }
                result.add(element);
            });
        }
        return result;
    }
}
