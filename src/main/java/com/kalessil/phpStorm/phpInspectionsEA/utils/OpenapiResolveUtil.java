package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiResolveUtil {
    @Nullable
    static public PsiElement resolveReference(@NotNull PsiReference reference) {
        try {
            return reference.resolve();
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @Nullable
    static public PhpType resolveType(@NotNull PhpTypedElement expression, @NotNull Project project) {
        PhpType result = null;
        try {
            /* workaround for https://youtrack.jetbrains.com/issue/WI-37013 */
            if (expression instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) expression;
                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    if (left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                        final PhpType leftType  = resolveType((PhpTypedElement) left, project);
                        final PhpType rightType = resolveType((PhpTypedElement) right, project);
                        if (leftType != null && rightType != null) {
                            result = new PhpType().add(leftType.filterNull()).add(rightType);
                        }
                    }
                }
            }
            /* default behaviour */
            result = result == null ? expression.getType().global(project) : result;
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            result = null;
        }
        return result;
    }

    @NotNull
    static public Collection<PhpClass> resolveClassesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getClassesByFQN(name);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @NotNull
    static public Collection<PhpClass> resolveInterfacesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getInterfacesByFQN(name);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @Nullable
    static public PhpClass resolveSuperClass(@NotNull PhpClass clazz) {
        try {
            return clazz.getSuperClass();
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @NotNull
    static public List<PhpClass> resolveImplementedInterfaces(@NotNull PhpClass clazz) {
        try {
            final PhpClass[] interfaces = clazz.getImplementedInterfaces();
            return interfaces == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(interfaces));
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @Nullable
    static public Method resolveMethod(@NotNull PhpClass clazz, @NotNull String methodName) {
        try {
            return clazz.findMethodByName(methodName);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @Nullable
    static public Field resolveField(@NotNull PhpClass clazz, @NotNull String fieldName) {
        try {
            return clazz.findFieldByName(fieldName, false);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }
}
