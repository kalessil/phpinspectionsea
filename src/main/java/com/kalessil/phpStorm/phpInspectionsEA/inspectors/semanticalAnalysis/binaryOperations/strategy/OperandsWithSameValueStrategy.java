package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

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

final public class OperandsWithSameValueStrategy {
    private static final String message = "Same value used in the operation (the operation is incorrect or can be simplified).";

    private final static Set<IElementType> targetOperations      = new HashSet<>();
    private final static Set<IElementType> targetInnerOperations = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opEQUAL);
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_EQUAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opGREATER);
        targetOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        targetOperations.add(PhpTokenTypes.opLESS);
        targetOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);

        targetInnerOperations.add(PhpTokenTypes.opPLUS);
        targetInnerOperations.add(PhpTokenTypes.opMINUS);
        targetInnerOperations.add(PhpTokenTypes.opMUL);
        targetInnerOperations.add(PhpTokenTypes.opDIV);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        if (targetOperations.contains(expression.getOperationType())) {
            final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
            if (left != null && right != null) {
                final PsiElement innerBinary = left instanceof BinaryExpression ? left : right;
                final PsiElement innerOther  = left instanceof BinaryExpression ? right : left;
                if (innerBinary instanceof BinaryExpression && ! (innerOther instanceof BinaryExpression)) {
                    final BinaryExpression binary = (BinaryExpression) innerBinary;
                    if (targetInnerOperations.contains(binary.getOperationType())) {
                        final PsiElement binaryLeft  = binary.getLeftOperand();
                        final PsiElement binaryRight = binary.getRightOperand();
                        if (binaryLeft != null && binaryRight != null) {
                            final boolean isTarget = OpenapiEquivalenceUtil.areEqual(binaryLeft, innerOther) ||
                                                     OpenapiEquivalenceUtil.areEqual(binaryRight, innerOther);
                            if (isTarget) {
                                holder.registerProblem(
                                        expression,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
