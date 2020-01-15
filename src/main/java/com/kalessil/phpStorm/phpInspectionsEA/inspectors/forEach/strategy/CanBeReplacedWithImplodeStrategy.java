package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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

final public class CanBeReplacedWithImplodeStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (candidate instanceof SelfAssignmentExpression) {
                final SelfAssignmentExpression assignment = (SelfAssignmentExpression) candidate;
                if (assignment.getOperationType() == PhpTokenTypes.opCONCAT_ASGN) {
                    /* concatenated value can be concatenation itself */
                    PsiElement concatenateValue = assignment.getValue();
                    if (concatenateValue instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) concatenateValue;
                        if (binary.getOperationType() == PhpTokenTypes.opCONCAT) {
                            final PsiElement left = binary.getLeftOperand();
                            if (left instanceof StringLiteralExpression && ((StringLiteralExpression) left).getFirstPsiChild() == null) {
                                concatenateValue = binary.getRightOperand();
                            }
                        }
                    }
                    /* now match */
                    final PsiElement loopValue = foreach.getValue();
                    if (loopValue != null && concatenateValue != null && OpenapiEquivalenceUtil.areEqual(loopValue, concatenateValue)) {
                        /* skip generators - the loop is performance-optimized */
                        final PsiElement source = foreach.getArray();
                        if (source instanceof PhpTypedElement) {
                            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) source, project);
                            if (resolved != null && resolved.filterUnknown().getTypes().contains("\\Generator")) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
