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
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import org.jetbrains.annotations.NotNull;

final public class EqualsInAssignmentContextStrategy {
    private static final String message = "It seems that '=' should be here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        /* general structure expectations */
        final PsiElement operation = expression.getOperation();
        if (null == operation || PhpTokenTypes.opEQUAL != operation.getNode().getElementType()) {
            return false;
        }

        /* analysis itself */
        if (expression.getParent() instanceof StatementImpl) {
            holder.registerProblem(operation, message, ProblemHighlightType.GENERIC_ERROR);
            return true;
        }

        return false;
    }
}
