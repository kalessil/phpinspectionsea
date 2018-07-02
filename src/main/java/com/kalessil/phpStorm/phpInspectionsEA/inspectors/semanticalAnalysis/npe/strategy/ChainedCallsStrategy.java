package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
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

    public static void apply(@NotNull Function function, @NotNull ProblemsHolder holder) {
        final Map<MethodReference, String> nullTested = new HashMap<>();
        PsiTreeUtil.findChildrenOfType(function, MethodReference.class).forEach(reference -> apply(reference, nullTested, holder));
        nullTested.clear();
    }

    private static void apply(
        @NotNull MethodReference reference,
        @NotNull Map<MethodReference, String> nullTestedReferences,
        @NotNull ProblemsHolder holder
    ) {
        final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
        if (OpenapiTypesUtil.is(operator, PhpTokenTypes.ARROW)) {
            final PsiElement base = reference.getFirstPsiChild();
            if (base instanceof FunctionReference) {
                final FunctionReference baseCall = (FunctionReference) base;
                final PhpType returnType         = OpenapiResolveUtil.resolveType(baseCall, holder.getProject());
                if (returnType != null) {
                    final String methodName = baseCall.getName();
                    for (final String resolvedType : returnType.filterUnknown().getTypes()) {
                        final String type = Types.getType(resolvedType);
                        if (type.equals(Types.strNull) || type.equals(Types.strVoid)) {
                            boolean isNullTested = false;
                            for (final Map.Entry<MethodReference, String> entry : nullTestedReferences.entrySet()) {
                                final String nullTestedMethodName = entry.getValue();
                                if (nullTestedMethodName != null && nullTestedMethodName.equals(methodName)) {
                                    final boolean areEqual = OpenapiEquivalenceUtil.areEqual(entry.getKey(), baseCall);
                                    if (areEqual) {
                                        isNullTested = true;
                                        break;
                                    }
                                }
                            }
                            if (!isNullTested) {
                                holder.registerProblem(operator, message);
                                break;
                            }
                        }
                    }
                }
            }

            /* collect null-tested references: only after main inspection! */
            final PsiElement parent = reference.getParent();
            if (parent instanceof BinaryExpression) {
                final BinaryExpression parentExpression = (BinaryExpression) parent;
                if (PhpTokenTypes.tsCOMPARE_OPS.contains(parentExpression.getOperationType())) {
                    final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(parentExpression, reference);
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
