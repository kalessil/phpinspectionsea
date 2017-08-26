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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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
        PsiTreeUtil.findChildrenOfType(method, MethodReference.class)
                .forEach(reference -> apply(reference, nullTestedReferences, holder));
        nullTestedReferences.clear();
    }

    private static void apply(
        @NotNull MethodReference reference,
        @NotNull Map<MethodReference, String> nullTestedReferences,
        @NotNull ProblemsHolder holder
    ) {
        final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
        if (operator != null && PhpTokenTypes.ARROW == operator.getNode().getElementType()) {
            final PsiElement base = reference.getFirstPsiChild();
            if (base instanceof FunctionReference) {
                final FunctionReference baseReference = (FunctionReference) base;
                final String methodName               = baseReference.getName();
                final PhpType types                   = baseReference.getType().global(holder.getProject()).filterUnknown();
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
                            holder.registerProblem(operator, message);
                            break;
                        }
                    }
                }
            }

            /* collect null-tested references: only after main inspection! */
            final PsiElement parent = reference.getParent();
            if (parent instanceof BinaryExpression) {
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
            } else if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                nullTestedReferences.put(reference, reference.getName());
            }
        }
    }
}
