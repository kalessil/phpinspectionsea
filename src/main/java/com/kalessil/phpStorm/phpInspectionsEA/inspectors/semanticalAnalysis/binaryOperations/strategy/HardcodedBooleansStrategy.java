package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

final public class HardcodedBooleansStrategy {
    private static final String message = "This boolean makes no sense or enforces the operation result.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result         = false;
        final PsiElement left  = expression.getLeftOperand();
        if (PhpLanguageUtil.isBoolean(left)) {
            holder.registerProblem(left, message, ProblemHighlightType.GENERIC_ERROR);
            result = true;
        }
        final PsiElement right = expression.getRightOperand();
        if (PhpLanguageUtil.isBoolean(right)) {
            holder.registerProblem(right, message, ProblemHighlightType.GENERIC_ERROR);
            result = true;
        }
        return result;
    }
}
