package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ChainedCallsStrategy {
    private static final String message = "Null pointer exception may occur here.";

    public static void apply(@NotNull Method method, @NotNull ProblemsHolder holder) {
        final Map<MethodReference, String> nullTestedReferences = new HashMap<>();
        for (final MethodReference reference : PsiTreeUtil.findChildrenOfType(method, MethodReference.class)) {
            apply(reference, nullTestedReferences, holder);
        }
        nullTestedReferences.clear();
    }

    private static void apply(
        @NotNull MethodReference reference,
        @NotNull Map<MethodReference, String> nullTestedReferences,
        @NotNull ProblemsHolder holder
    ) {
        final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
        final PsiElement base     = operator == null ? null : reference.getFirstPsiChild();
        final PsiElement parent   = operator == null ? null : reference.getParent();

        /* inspect NPEs */
        if (base instanceof FunctionReference && PhpTokenTypes.ARROW == operator.getNode().getElementType()) {
            final FunctionReference baseReference = (FunctionReference) base;
            final String methodName              = baseReference.getName();
            final PhpType types                  = baseReference.getType().global(holder.getProject()).filterUnknown();
            for (final String resolvedType : types.getTypes()) {
                final String type = Types.getType(resolvedType);
                if (type.equals(Types.strNull) || type.equals(Types.strVoid)) {
                    boolean isNullTested = false;
                    for (final MethodReference nullTestedReference : nullTestedReferences.keySet()) {
                        final String nullTestedMethodName = nullTestedReferences.get(nullTestedReference);
                        if (
                            nullTestedMethodName != null && nullTestedMethodName.equals(methodName) &&
                            PsiEquivalenceUtil.areElementsEquivalent(nullTestedReference, baseReference)
                        ) {
                            isNullTested = true;
                            break;
                        }
                    }
                    if (!isNullTested) {
                        holder.registerProblem(operator, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        break;
                    }
                }
            }
        }

        /* collect null-tested references: only after main inspection! */
        if (parent instanceof BinaryExpression && PhpTokenTypes.ARROW == operator.getNode().getElementType()) {
            final BinaryExpression parentExpression = (BinaryExpression) parent;
            final IElementType parentOperation      = parentExpression.getOperationType();
            if (PhpTokenTypes.tsCOMPARE_OPS.contains(parentOperation)) {
                PsiElement secondOperand = parentExpression.getLeftOperand();
                if (secondOperand == reference) {
                    secondOperand = parentExpression.getRightOperand();
                }
                if (PhpLanguageUtil.isNull(secondOperand)) {
                    nullTestedReferences.put(reference, reference.getName());
                }
            }
        }
    }
}
