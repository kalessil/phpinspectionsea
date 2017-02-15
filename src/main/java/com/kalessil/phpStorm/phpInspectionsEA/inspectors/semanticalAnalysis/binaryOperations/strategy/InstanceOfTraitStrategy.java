package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class InstanceOfTraitStrategy {
    private static final String message = "instanceof against traits returns 'false'.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        /* general structure expectations */
        final PsiElement operation = expression.getOperation();
        if (null == operation || PhpTokenTypes.kwINSTANCEOF != operation.getNode().getElementType()) {
            return false;
        }
        final PsiElement right = expression.getRightOperand();
        if (!(right instanceof ClassReference) && !(right instanceof ClassConstantReference)) {
            return false;
        }

        /* getting class from invariant constructs  */
        PsiElement resolved = null;
        if (right instanceof ClassReference) {
            resolved = ((ClassReference) right).resolve();
        }
        if (right instanceof ClassConstantReference) {
            final ClassConstantReference ref = (ClassConstantReference) right;
            final PsiElement classReference  = ref.getClassReference();
            final String constantName        = ref.getName();
            if (null != constantName && constantName.equals("class") && classReference instanceof ClassReference) {
                resolved = ((ClassReference) classReference).resolve();
            }
        }

        /* analysis itself */
        if (resolved instanceof PhpClass && ((PhpClass) resolved).isTrait()) {
            holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR);
            return true;
        }

        return false;
    }
}
