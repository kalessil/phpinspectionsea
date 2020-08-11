package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

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
            final PhpType left = extract(expression.getLeftOperand(), holder);
            if (! left.isEmpty() && ! left.hasUnknown()) {
                final PhpType right = extract(expression.getRightOperand(), holder);
                if (! right.isEmpty() && ! right.hasUnknown()) {
                    final Set<String> leftTypes  = left.getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                    final Set<String> rightTypes = right.getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                    final boolean hasMixed       = leftTypes.contains(Types.strMixed) || rightTypes.contains(Types.strMixed);
                    if (! hasMixed) {
                        final boolean isIntersecting = rightTypes.stream().anyMatch(leftTypes::contains);
                        if (result = ! isIntersecting) {
                            if (operation == PhpTokenTypes.opIDENTICAL) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            } else {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            }
                        }
                    }
                    leftTypes.clear();
                    rightTypes.clear();
                }
            }
        }
        return result;
    }

    @NotNull
    private static PhpType extract(@Nullable PsiElement expression, @NotNull ProblemsHolder holder) {
        if (expression instanceof PhpTypedElement) {
            if (expression instanceof FunctionReference) {
                final FunctionReference reference = (FunctionReference) expression;
                final PsiElement resolved         = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof Function) {
                    final Function function = (Function) resolved;
                    /* rely on implicitly declared return types */
                    if (OpenapiElementsUtil.getReturnType(function) != null) {
                        final PhpType type = OpenapiResolveUtil.resolveType(reference, holder.getProject());
                        if (type != null) {
                            return type;
                        }
                    }
                    /* rely on narrowing types in our types resolving layer (PhpDoc is required) */
                    final PhpDocComment annotations = function.getDocComment();
                    if (annotations != null && annotations.getReturnTag() != null) {
                        final PhpType type = OpenapiResolveUtil.resolveType(reference, holder.getProject());
                        if (type != null) {
                            return type;
                        }
                    }
                }
            } else if (expression instanceof FieldReference) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference((FieldReference) expression);
                if (resolved instanceof Field && ! OpenapiResolveUtil.resolveDeclaredType((Field) resolved).isEmpty()) {
                    final PhpType type = OpenapiResolveUtil.resolveType((FieldReference) expression, holder.getProject());
                    if (type != null) {
                        return type;
                    }
                }
            } else if (expression instanceof StringLiteralExpression) {
                return PhpType.STRING;
            } else if (expression instanceof ArrayCreationExpression) {
                return PhpType.ARRAY;
            } else if (
                expression instanceof ConstantReference ||
                expression instanceof ClassConstantReference ||
                OpenapiTypesUtil.isNumber(expression)
            ) {
                final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, holder.getProject());
                if (type != null) {
                    return type;
                }
            }
        }
        return new PhpType();
    }
}