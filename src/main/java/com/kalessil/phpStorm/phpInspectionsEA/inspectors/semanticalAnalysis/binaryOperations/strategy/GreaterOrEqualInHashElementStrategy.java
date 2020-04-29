package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class GreaterOrEqualInHashElementStrategy {
    private static final String message = "It seems that '=>' should be here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        /* general structure expectations */
        final PsiElement operation = expression.getOperation();
        if (null == operation || PhpTokenTypes.opGREATER_OR_EQUAL != operation.getNode().getElementType()) {
            return false;
        }
        final PsiElement left = expression.getLeftOperand();
        if (!(left instanceof StringLiteralExpression)) {
            return false;
        }

        /* analysis itself */
        final PsiElement parent = expression.getParent();
        if (null != parent && parent.getParent() instanceof ArrayCreationExpression) {
            holder.registerProblem(
                    operation,
                    MessagesPresentationUtil.prefixWithEa(message)
            );
            return true;
        }

        return false;
    }
}
