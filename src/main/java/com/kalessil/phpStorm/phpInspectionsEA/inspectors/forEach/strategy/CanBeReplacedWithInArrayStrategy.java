package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class CanBeReplacedWithInArrayStrategy extends AbstractStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        /* expecting if with 2 statements (assignment and break) */
        final PsiElement value = foreach.getValue();
        if (value != null && expression instanceof If) {
            final If ifStatement = (If) expression;
            if (! ExpressionSemanticUtil.hasAlternativeBranches((If) expression)) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 2) {
                    final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                    if (last instanceof PhpBreak) {
                        final PsiElement first = ExpressionSemanticUtil.getFirstStatement(body);
                        if (OpenapiTypesUtil.isStatementImpl(first)) {
                            final PsiElement candidate = first.getFirstChild();
                            if (OpenapiTypesUtil.isAssignment(candidate)) {
                                return PhpLanguageUtil.isTrue(((AssignmentExpression) candidate).getValue()) &&
                                       isTargetCondition(value, ifStatement.getCondition());
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static private boolean isTargetCondition(@NotNull PsiElement value, @Nullable PsiElement condition) {
        if (condition instanceof BinaryExpression) {
            final BinaryExpression binary = (BinaryExpression) condition;
            final IElementType operation  = binary.getOperationType();
            if (operation == PhpTokenTypes.opEQUAL || operation == PhpTokenTypes.opIDENTICAL) {
                final PsiElement left  = binary.getLeftOperand();
                final PsiElement right = binary.getRightOperand();
                if (left != null && right != null) {
                    return OpenapiEquivalenceUtil.areEqual(left, value) || OpenapiEquivalenceUtil.areEqual(right, value);
                }
            }
        }
        return false;
    }
}
