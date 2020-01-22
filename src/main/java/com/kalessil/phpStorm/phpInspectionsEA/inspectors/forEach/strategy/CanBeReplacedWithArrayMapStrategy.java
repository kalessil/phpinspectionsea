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

final public class CanBeReplacedWithArrayMapStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (OpenapiTypesUtil.isAssignment(candidate)) {
                final AssignmentExpression assignment = (AssignmentExpression) candidate;
                final PsiElement assignmentValue      = assignment.getValue();
                PsiElement usedValue                  = null;
                /* extract if value gets type casted or processed with a call */
                if (OpenapiTypesUtil.isFunctionReference(assignmentValue)) {
                    final PsiElement[] arguments = ((FunctionReference) assignmentValue).getParameters();
                    if (arguments.length == 1) {
                        usedValue = arguments[0];
                    }
                } else if (assignmentValue instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) assignmentValue;
                    final PsiElement operator   = unary.getOperation();
                    if (
                            OpenapiTypesUtil.is(operator, PhpTokenTypes.opINTEGER_CAST) ||
                            OpenapiTypesUtil.is(operator, PhpTokenTypes.opFLOAT_CAST) ||
                            OpenapiTypesUtil.is(operator, PhpTokenTypes.opSTRING_CAST) ||
                            OpenapiTypesUtil.is(operator, PhpTokenTypes.opBOOLEAN_CAST)
                    ) {
                        usedValue = unary.getValue();
                    }
                }
                /* now match */
                if (usedValue != null) {
                    final PsiElement loopValue = foreach.getValue();
                    if (loopValue != null && OpenapiEquivalenceUtil.areEqual(usedValue, loopValue)) {
                        final PsiElement assignmentContainer = assignment.getVariable();
                        if (assignmentContainer instanceof ArrayAccessExpression) {
                            final ArrayIndex indexHolder = ((ArrayAccessExpression) assignmentContainer).getIndex();
                            final PsiElement index       = indexHolder == null ? null : indexHolder.getValue();
                            final PsiElement loopIndex   = foreach.getKey();
                            if (index != null && loopIndex != null && OpenapiEquivalenceUtil.areEqual(index, loopIndex)) {
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
            }
        }
        return false;
    }
}
