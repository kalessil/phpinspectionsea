package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
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

final public class CanBeReplacedWithArrayProductStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (candidate instanceof SelfAssignmentExpression) {
                final SelfAssignmentExpression assignment = (SelfAssignmentExpression) candidate;
                final IElementType operator               = assignment.getOperationType();
                if (operator == PhpTokenTypes.opMUL_ASGN && assignment.getVariable() instanceof Variable) {
                    final PsiElement accumulatedValue = assignment.getValue();
                    final PsiElement loopValue        = foreach.getValue();
                    if (loopValue != null && accumulatedValue != null && OpenapiEquivalenceUtil.areEqual(loopValue, accumulatedValue)) {
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
