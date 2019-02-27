package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TypesIntersectionStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result               = false;
        final IElementType operation = expression.getOperationType();
        if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
            final PhpType left = extract(expression.getLeftOperand());
            if (!left.isEmpty() && !left.hasUnknown()) {
                final PhpType right = extract(expression.getRightOperand());
                if (!right.isEmpty() && !right.hasUnknown()) {
                    final Set<String> leftTypes  = left.getTypes();
                    final boolean isIntersecting = right.getTypes().stream().anyMatch(leftTypes::contains);
                    if (result = !isIntersecting) {
                        if (operation == PhpTokenTypes.opIDENTICAL) {
                            holder.registerProblem(expression, String.format(messageAlwaysFalse, expression.getText()));
                        } else {
                            holder.registerProblem(expression, String.format(messageAlwaysTrue, expression.getText()));
                        }
                    }
                }
            }
        }
        return result;
    }

    private static PhpType extract(@Nullable PsiElement expression) {
        if (expression instanceof PhpTypedElement) {
            if (expression instanceof FunctionReference) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference((FunctionReference) expression);
                if (resolved instanceof Function && OpenapiElementsUtil.getReturnType((Function) resolved) != null) {
                    final PhpType type = OpenapiResolveUtil.resolveType((FunctionReference) expression, expression.getProject());
                    if (type != null) {
                        return type;
                    }
                }
            } else if (expression instanceof StringLiteralExpression) {
                return PhpType.STRING;
            } else if (
                expression instanceof ArrayCreationExpression ||
                expression instanceof ConstantReference ||
                expression instanceof ClassConstantReference ||
                OpenapiTypesUtil.isNumber(expression)
            ) {
                final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, expression.getProject());
                if (type != null) {
                    return type;
                }
            }
        }
        return new PhpType();
    }
}