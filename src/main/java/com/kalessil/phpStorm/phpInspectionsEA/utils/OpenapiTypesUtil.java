package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import org.jetbrains.annotations.Nullable;

final public class OpenapiTypesUtil {
    /* Overhead identifying lambdas */
    static public boolean isLambda(@Nullable PsiElement expression) {
        if (expression instanceof PhpExpressionImpl) {
            PsiElement value = ((PhpExpressionImpl) expression).getValue();
            return value instanceof Function && ((Function) value).isClosure();
        }

        return expression instanceof Function && ((Function) expression).isClosure();
    }
}
