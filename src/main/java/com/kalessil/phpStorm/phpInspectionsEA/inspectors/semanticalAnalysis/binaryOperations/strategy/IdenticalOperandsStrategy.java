package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class IdenticalOperandsStrategy {
    private static final String message = "Left and right operands are identical.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
        final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
        if (null == left || null == right) {
            return false;
        }

        if (PsiEquivalenceUtil.areElementsEquivalent(left, right)) {
            holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR);
            return true;
        }

        return false;
    }
}
