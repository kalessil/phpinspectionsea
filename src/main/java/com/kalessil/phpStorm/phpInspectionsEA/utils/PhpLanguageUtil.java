package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import org.jetbrains.annotations.Nullable;

final public class PhpLanguageUtil {
    public static boolean isNull(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return null != name && name.equalsIgnoreCase("null");
        }
        return false;
    }

    public static boolean isTrue(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return null != name && name.equalsIgnoreCase("true");
        }
        return false;
    }

    public static boolean isFalse(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return null != name && name.equalsIgnoreCase("false");
        }
        return false;
    }

    public static boolean isBoolean(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return null != name && (name.equalsIgnoreCase("false") || name.equalsIgnoreCase("true"));
        }
        return false;
    }
}
