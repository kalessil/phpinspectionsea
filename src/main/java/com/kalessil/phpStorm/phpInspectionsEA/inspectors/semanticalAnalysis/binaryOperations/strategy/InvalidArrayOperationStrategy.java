package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

final public class InvalidArrayOperationStrategy {
    private static final String message = "Operation on an array doesn't make much sense here.";

    private final static Set<IElementType> validOperations = new HashSet<>();
    static {
        validOperations.add(PhpTokenTypes.opCOALESCE);
        validOperations.add(PhpTokenTypes.opPLUS);
        validOperations.add(PhpTokenTypes.opIDENTICAL);
        validOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        validOperations.add(PhpTokenTypes.opEQUAL);
        validOperations.add(PhpTokenTypes.opNOT_EQUAL);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement operation = expression.getOperation();
        if (operation != null && !validOperations.contains(operation.getNode().getElementType())) {
            final boolean isTarget = expression.getLeftOperand() instanceof ArrayCreationExpression ||
                                     expression.getRightOperand() instanceof ArrayCreationExpression;
            if (isTarget) {
                holder.registerProblem(operation, ReportingUtil.wrapReportedMessage(message));
                return true;
            }
        }
        return false;
    }
}
