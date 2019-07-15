package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
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

final public class ConstantConditionsIsNumericStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";

    public static boolean apply(@NotNull FunctionReference reference, @NotNull ProblemsHolder holder) {
        final String functionName = reference.getName();
        if (functionName != null && functionName.equals("is_numeric")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 1) {
                final PsiElement argument = arguments[0];
                if (argument instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) argument).getOperation();
                    final boolean isTarget     = OpenapiTypesUtil.is(operation, PhpTokenTypes.opFLOAT_CAST) ||
                                                 OpenapiTypesUtil.is(operation, PhpTokenTypes.opINTEGER_CAST);
                    if (isTarget) {
                        holder.registerProblem(reference, String.format(messageAlwaysTrue, reference.getText()));
                        return true;
                    }
                } else if (argument instanceof FunctionReference) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((FunctionReference) argument);
                    if (resolved instanceof Function && OpenapiElementsUtil.getReturnType((Function) resolved) != null) {
                        final PhpType type = OpenapiResolveUtil.resolveType((FunctionReference) argument, holder.getProject());
                        if (type != null && type.size() == 1) {
                            final boolean isTarget = type.equals(PhpType.INT) || type.equals(PhpType.FLOAT);
                            if (isTarget) {
                                holder.registerProblem(reference, String.format(messageAlwaysTrue, reference.getText()));
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
