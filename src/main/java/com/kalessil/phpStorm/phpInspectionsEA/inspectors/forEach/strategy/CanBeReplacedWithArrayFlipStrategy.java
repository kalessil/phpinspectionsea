package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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

final public class CanBeReplacedWithArrayFlipStrategy {
    static public boolean apply(@NotNull ForeachStatement foreach, @NotNull PsiElement expression, @NotNull Project project) {
        if (OpenapiTypesUtil.isStatementImpl(expression)) {
            final PsiElement candidate = expression.getFirstChild();
            if (OpenapiTypesUtil.isAssignment(candidate)) {
                final AssignmentExpression assignment = (AssignmentExpression) candidate;
                final PsiElement assignmentValue      = assignment.getValue();
                final PsiElement loopIndex            = foreach.getKey();
                if (loopIndex != null && assignmentValue != null && OpenapiEquivalenceUtil.areEqual(loopIndex, assignmentValue)) {
                    final PsiElement container = assignment.getVariable();
                    if (container instanceof ArrayAccessExpression) {
                        final ArrayAccessExpression storage = (ArrayAccessExpression) container;
                        final ArrayIndex storageIndex       = storage.getIndex();
                        if (storageIndex != null) {
                            final PsiElement usedIndex = storageIndex.getValue();
                            final PsiElement loopValue = foreach.getValue();
                            if (loopValue != null && usedIndex != null && OpenapiEquivalenceUtil.areEqual(usedIndex, loopValue)) {
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
