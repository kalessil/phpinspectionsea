package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
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
                final AssignmentExpression assignment = (AssignmentExpression) candidate;
                final PsiElement container            = assignment.getVariable();
                if (container instanceof ArrayAccessExpression) {
                    final ArrayAccessExpression access = (ArrayAccessExpression) container;
                    final boolean hasIndex             = access.getIndex() != null && access.getIndex().getValue() != null;
                    if (! hasIndex) {
                        final PsiElement assignedBase;
                        final PsiElement assignedValue = assignment.getValue();
                        if (assignedValue instanceof ArrayAccessExpression) {
                            assignedBase = ((ArrayAccessExpression) assignedValue).getValue();
                        } else if (assignedValue instanceof FieldReference) {
                            final boolean supportsObjects = PhpLanguageLevel.get(project).atLeast(PhpLanguageLevel.PHP700);
                            assignedBase = supportsObjects ? ((FieldReference) assignedValue).getClassReference() : null;
                        } else {
                            assignedBase = null;
                        }
                        if (assignedBase != null) {
                            final PsiElement loopValue = foreach.getValue();
                            return loopValue != null && OpenapiEquivalenceUtil.areEqual(loopValue, assignedBase);
                        }
                    }
                }
            }
        }
        return false;
    }
}
