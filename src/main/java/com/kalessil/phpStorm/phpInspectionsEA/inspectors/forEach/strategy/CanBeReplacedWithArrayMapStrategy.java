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
                final PsiElement loopSource = foreach.getArray();
                final PsiElement loopIndex  = foreach.getKey();
                final PsiElement loopValue  = foreach.getValue();
                if (loopSource != null && loopIndex != null && loopValue != null) {
                    final AssignmentExpression assignment = (AssignmentExpression) candidate;
                    final PsiElement assignedValue        = assignment.getValue();
                    final PsiElement assignmentStorage    = assignment.getVariable();
                    if (assignedValue != null && assignmentStorage instanceof ArrayAccessExpression) {
                        PsiElement extractedValue = null;
                        if (OpenapiTypesUtil.isFunctionReference(assignedValue)) {
                            final PsiElement[] arguments = ((FunctionReference) assignedValue).getParameters();
                            if (arguments.length == 1) {
                                extractedValue = arguments[0];
                            }
                        } else if (assignedValue instanceof UnaryExpression) {
                            final UnaryExpression unary = (UnaryExpression) assignedValue;
                            final PsiElement operator   = unary.getOperation();
                            if (
                                    OpenapiTypesUtil.is(operator, PhpTokenTypes.opINTEGER_CAST) ||
                                    OpenapiTypesUtil.is(operator, PhpTokenTypes.opFLOAT_CAST) ||
                                    OpenapiTypesUtil.is(operator, PhpTokenTypes.opSTRING_CAST) ||
                                    OpenapiTypesUtil.is(operator, PhpTokenTypes.opBOOLEAN_CAST)
                            ) {
                                extractedValue = unary.getValue();
                            }
                        }
                        /* now match */
                        if (extractedValue != null) {
                            final ArrayIndex keyHolder = ((ArrayAccessExpression) assignmentStorage).getIndex();
                            final PsiElement key       = keyHolder == null ? null : keyHolder.getValue();
                            if (key != null && OpenapiEquivalenceUtil.areEqual(key, loopIndex)) {
                                /* false-positives: generators */
                                final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) loopSource, project);
                                if (resolved != null && resolved.filterUnknown().getTypes().contains("\\Generator")) {
                                    return false;
                                }
                                return isArrayElement(extractedValue, loopSource, loopIndex) || isArrayValue(extractedValue, loopValue);
                            }
                        }
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
