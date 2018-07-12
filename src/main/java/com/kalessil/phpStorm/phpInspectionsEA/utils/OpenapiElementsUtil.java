package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    final private static Method methodReturnType;
    static {
        try {
            methodReturnType = Function.class.getDeclaredMethod("getReturnType");
        } catch (final NoSuchMethodException failure) {
            throw new RuntimeException(failure);
        }
    }

    final private static Method phpInstructionGetPredecessors;
    static {
        try {
            phpInstructionGetPredecessors = PhpInstruction.class.getDeclaredMethod("getPredecessors");
        } catch (final NoSuchMethodException failure) {
            throw new RuntimeException(failure);
        }
    }

    @Nullable
    static public PsiElement getReturnType(@NotNull Function function) {
        PsiElement result;
        try {
            /* PS 2017.3 has changed return type from PsiElement to PhpReturnType, so we use reflection here */
            result = (PsiElement) methodReturnType.invoke(function);
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
        Collection<PhpInstruction> result;
        try {
            /* PS 2017.3 has changed return type from Collection<> to List<>, so we use reflection here */
            result = (Collection<PhpInstruction>) phpInstructionGetPredecessors.invoke(instruction);
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
