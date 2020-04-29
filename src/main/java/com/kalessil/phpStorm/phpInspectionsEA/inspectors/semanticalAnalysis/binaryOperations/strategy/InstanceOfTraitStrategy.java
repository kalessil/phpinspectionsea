package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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

    private static final Set<String> lateBindingSymbols = new HashSet<>();
    static {
        lateBindingSymbols.add("self");
        lateBindingSymbols.add("static");
        lateBindingSymbols.add("$this");
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        /* general structure expectations */
        if (expression.getOperationType() != PhpTokenTypes.kwINSTANCEOF) {
            return false;
        }
        final PsiElement right = expression.getRightOperand();
        if (!(right instanceof ClassReference) && !(right instanceof ClassConstantReference)) {
            return false;
        }
        /* $this, self, static are referencing to host classes, skip the case */
        if (lateBindingSymbols.contains(right.getText())) {
            return false;
        }

        /* getting class from invariant constructs  */
        PsiElement resolved = null;
        if (right instanceof ClassReference) {
            resolved = OpenapiResolveUtil.resolveReference((ClassReference) right);
        }
        if (right instanceof ClassConstantReference) {
            final ClassConstantReference ref = (ClassConstantReference) right;
            final PsiElement classReference  = ref.getClassReference();
            final String constantName        = ref.getName();
            if (null != constantName && constantName.equals("class") && classReference instanceof ClassReference) {
                resolved = OpenapiResolveUtil.resolveReference((ClassReference) classReference);
            }
        }

        /* analysis itself */
        if (resolved instanceof PhpClass && ((PhpClass) resolved).isTrait()) {
            holder.registerProblem(
                    expression,
                    MessagesPresentationUtil.prefixWithEa(message)
            );
            return true;
        }

        return false;
    }
}
