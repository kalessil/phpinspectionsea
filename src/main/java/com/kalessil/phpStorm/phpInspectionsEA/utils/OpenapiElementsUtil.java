package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        } catch (NoSuchMethodException failure) {
            throw new RuntimeException(failure);
        }
    }

    @Nullable
    static public PsiElement getReturnType(@NotNull Function function) {
        PsiElement result;
        try {
            /* PS 2017.3 has changed return type from PsiElement to PhpReturnType, so we use reflection here */
            result = (PsiElement) methodReturnType.invoke(function);
        } catch (IllegalAccessException|InvocationTargetException failure) {
            throw new RuntimeException(failure);
        }
        return result;
    }
}
