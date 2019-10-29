package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PossiblyArrayHashElementDeclarationStrategy {
    private static final String message = "It was probably intended to use '=>' here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement operation = expression.getOperation();
        if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opGREATER_OR_EQUAL)) {
            final PsiElement left = expression.getLeftOperand();
            if (left instanceof StringLiteralExpression) {
                final PsiElement parent = expression.getParent();
                if (parent != null && parent.getParent() instanceof ArrayCreationExpression) {
                    holder.registerProblem(
                            operation,
                            ReportingUtil.wrapReportedMessage(message)
                    );
                    return true;
                }
            }
        }
        return false;
    }
}
