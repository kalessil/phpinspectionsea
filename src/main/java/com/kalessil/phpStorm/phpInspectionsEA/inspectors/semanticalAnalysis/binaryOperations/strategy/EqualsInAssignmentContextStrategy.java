package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class EqualsInAssignmentContextStrategy {
    private static final String message = "It seems that '=' should be here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement operation = expression.getOperation();
        if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opEQUAL)) {
            final PsiElement parent = expression.getParent();
            if (OpenapiTypesUtil.isStatementImpl(parent)) {
                holder.registerProblem(
                        operation,
                        MessagesPresentationUtil.prefixWithEa(message)
                );
                return true;
            }
        }
        return false;
    }
}
