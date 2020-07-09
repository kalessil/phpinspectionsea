package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
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

final public class CanBeReplacedWithArrayProductStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (candidate instanceof SelfAssignmentExpression) {
                final PsiElement loopSource = foreach.getArray();
                final PsiElement loopIndex  = foreach.getKey();
                final PsiElement loopValue  = foreach.getValue();
                if (loopSource != null && loopValue != null) {
                    final SelfAssignmentExpression assignment = (SelfAssignmentExpression) candidate;
                    final PsiElement assignmentStorage        = assignment.getValue();
                    final PsiElement assignedValue            = assignment.getValue();
                    if (assignedValue != null && assignmentStorage != null && assignment.getOperationType() == PhpTokenTypes.opMUL_ASGN) {
                        /* false-positive: generators */
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) loopSource, project);
                        if (resolved != null && resolved.filterUnknown().getTypes().contains("\\Generator")) {
                            return false;
                        }
                        return isArrayValue(assignedValue, loopValue) || (loopIndex != null && isArrayElement(assignedValue, loopSource, loopIndex));
                    }
                }
            }
        }
        return false;
    }

    static private boolean isArrayElement(@NotNull PsiElement candidate, @NotNull PsiElement source, @NotNull PsiElement index) {
        if (candidate instanceof ArrayAccessExpression) {
            final ArrayAccessExpression access = (ArrayAccessExpression) candidate;
            final PsiElement base              = access.getValue();
            final ArrayIndex keyHolder         = access.getIndex();
            final PsiElement key               = keyHolder == null ? null : keyHolder.getValue();
            if (base != null && key != null) {
                return OpenapiEquivalenceUtil.areEqual(base, source) && OpenapiEquivalenceUtil.areEqual(key, index);
            }
        }
        return false;
    }

    static private boolean isArrayValue(@NotNull PsiElement candidate, @NotNull PsiElement value) {
        return OpenapiEquivalenceUtil.areEqual(candidate, value);
    }
}
