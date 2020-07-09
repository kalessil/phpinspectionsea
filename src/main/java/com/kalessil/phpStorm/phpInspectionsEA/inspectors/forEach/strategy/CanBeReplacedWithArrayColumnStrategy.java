package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

final public class CanBeReplacedWithArrayColumnStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (OpenapiTypesUtil.isAssignment(candidate)) {
                final PsiElement loopSource = foreach.getArray();
                final PsiElement loopIndex  = foreach.getKey();
                final PsiElement loopValue  = foreach.getValue();
                if (loopSource != null && loopIndex != null && loopValue != null) {
                    final AssignmentExpression assignment = (AssignmentExpression) candidate;
                    final PsiElement assignmentStorage    = assignment.getVariable();
                    final PsiElement assignedValue        = assignment.getValue();
                    if (assignmentStorage instanceof ArrayAccessExpression && assignedValue != null) {
                        final ArrayIndex keyHolder = ((ArrayAccessExpression) assignmentStorage).getIndex();
                        final boolean hasKey       = keyHolder != null && keyHolder.getValue() != null;
                        if (! hasKey) {
                            final PsiElement extractedValue;
                            if (assignedValue instanceof ArrayAccessExpression) {
                                extractedValue = ((ArrayAccessExpression) assignedValue).getValue();
                            } else if (assignedValue instanceof FieldReference) {
                                final boolean supportsObjects = PhpLanguageLevel.get(project).atLeast(PhpLanguageLevel.PHP700);
                                extractedValue = supportsObjects ? ((FieldReference) assignedValue).getClassReference() : null;
                            } else {
                                extractedValue = null;
                            }
                            if (extractedValue != null) {
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
