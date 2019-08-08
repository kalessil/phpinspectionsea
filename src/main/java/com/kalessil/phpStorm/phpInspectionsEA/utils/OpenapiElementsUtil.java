package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiElementsUtil {
    @Nullable
    private static Method functionReturnType;
    static {
        try {
            functionReturnType = Function.class.getDeclaredMethod("getReturnType");
        } catch (final NoSuchMethodException failure) {
            functionReturnType = null;
        }
    }

    @Nullable
    private static Method phpInstructionPredecessors;
    static {
        try {
            phpInstructionPredecessors = PhpInstruction.class.getDeclaredMethod("getPredecessors");
        } catch (final NoSuchMethodException failure) {
            phpInstructionPredecessors = null;
        }
    }

    @Nullable
    private static Method fieldDeclaredType;
    static {
        try {
            fieldDeclaredType = Field.class.getDeclaredMethod("getDeclaredType");
        } catch (final NoSuchMethodException failure) {
            fieldDeclaredType = null;
        }
    }

    @Nullable
    static public PsiElement getReturnType(@NotNull Function function) {
        final PsiElement result;
        try {
            /* BC: PS 2017.3 has changed return type from PsiElement to PhpReturnType */
            result = functionReturnType == null
                    ? null
                    : (PsiElement) functionReturnType.invoke(function);
        } catch (final IllegalAccessException failure) {
            throw new RuntimeException(failure);
        } catch (final InvocationTargetException failure) {
            final Throwable cause = failure.getTargetException();
            throw (cause instanceof RuntimeException ? (RuntimeException)cause : new RuntimeException(cause));
        }
        return result;
    }

    @NotNull
    static PhpType getDeclaredType(@NotNull Field field) {
        final PhpType result;
        try {
            /* FC: PS 2019.2 has introduced typed properties, and we need to gracefully access the types information */
            result = fieldDeclaredType == null
                    ? PhpType.EMPTY
                    : (PhpType) fieldDeclaredType.invoke(field);
        } catch (final IllegalAccessException failure) {
            throw new RuntimeException(failure);
        } catch (final InvocationTargetException failure) {
            final Throwable cause = failure.getTargetException();
            throw (cause instanceof RuntimeException ? (RuntimeException)cause : new RuntimeException(cause));
        }
        return result;
    }

    @NotNull
    static public Collection<PhpInstruction> getPredecessors(@NotNull PhpInstruction instruction) {
        final Collection<PhpInstruction> result;
        try {
            /* BC: PS 2017.3 has changed return type from Collection<...> to List<...> */
            result = phpInstructionPredecessors == null
                    ? new ArrayList<>()
                    : (Collection<PhpInstruction>) phpInstructionPredecessors.invoke(instruction);
        } catch (final IllegalAccessException failure) {
            throw new RuntimeException(failure);
        } catch (final InvocationTargetException failure) {
            final Throwable cause = failure.getTargetException();
            throw (cause instanceof RuntimeException ? (RuntimeException)cause : new RuntimeException(cause));
        }
        return result;
    }

    @Nullable
    static public PsiElement getSecondOperand(@NotNull BinaryExpression binary, @NotNull PsiElement first) {
        final PsiElement left = binary.getLeftOperand();
        return left == first ? binary.getRightOperand() : left;
    }
}
