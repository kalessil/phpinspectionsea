package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SelfAssignmentStrategy {
    private static final String message = "Related operation being applied to the same variable (probably merging issues).";

    static private final Map<IElementType, IElementType> mapping = new HashMap<>();
    static {
        mapping.put(PhpTokenTypes.opPLUS_ASGN,        PhpTokenTypes.opPLUS);
        mapping.put(PhpTokenTypes.opMINUS_ASGN,       PhpTokenTypes.opMINUS);
        mapping.put(PhpTokenTypes.opMUL_ASGN,         PhpTokenTypes.opMUL);
        mapping.put(PhpTokenTypes.opDIV_ASGN,         PhpTokenTypes.opDIV);
        mapping.put(PhpTokenTypes.opREM_ASGN,         PhpTokenTypes.opREM);
        mapping.put(PhpTokenTypes.opCONCAT_ASGN,      PhpTokenTypes.opCONCAT);
        mapping.put(PhpTokenTypes.opBIT_AND_ASGN,     PhpTokenTypes.opBIT_AND);
        mapping.put(PhpTokenTypes.opBIT_OR_ASGN,      PhpTokenTypes.opBIT_OR);
        mapping.put(PhpTokenTypes.opBIT_XOR_ASGN,     PhpTokenTypes.opBIT_XOR);
        mapping.put(PhpTokenTypes.opSHIFT_LEFT_ASGN,  PhpTokenTypes.opSHIFT_LEFT);
        mapping.put(PhpTokenTypes.opSHIFT_RIGHT_ASGN, PhpTokenTypes.opSHIFT_RIGHT);
    }

    static public void apply(@NotNull SelfAssignmentExpression expression, @NotNull ProblemsHolder holder) {
        /* verify self-assignment operator */
        final IElementType assignOperator = expression.getOperationType();
        if (!mapping.containsKey(assignOperator)) {
            return;
        }

        /* verify if the expression is complete and has needed structure */
        final PsiElement variable = expression.getVariable();
        final PsiElement value    = expression.getValue();
        if (null == value || null == variable || !(value instanceof BinaryExpression)) {
            return;
        }
        final BinaryExpression valueExpression = (BinaryExpression) value;

        /* check if assignment value is complete */
        final PsiElement valueOperation = valueExpression.getOperation();
        final PsiElement valueLeftPart  = valueExpression.getLeftOperand();
        if (null == valueOperation || null == valueLeftPart) {
            return;
        }

        /* now analysis itself */
        if (
            OpenapiTypesUtil.is(valueOperation, mapping.get(assignOperator)) &&
            OpenapiEquivalenceUtil.areEqual(variable, valueLeftPart)
        ) {
            holder.registerProblem(expression, message);
        }
    }
}
