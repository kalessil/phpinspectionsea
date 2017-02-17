package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
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

    /* Filters self-assignments */
    static public boolean isAssignment(@Nullable PsiElement expression) {
        return
            expression instanceof AssignmentExpression &&
            !(expression instanceof SelfAssignmentExpression) &&
            !(expression instanceof MultiassignmentExpression);
    }

    /* Filters method references */
    static public boolean isFunctionReference(@Nullable PsiElement expression) {
        return expression instanceof FunctionReference && !(expression instanceof MethodReference);
    }

    /* Simplification detecting loops; interface already available in EAPs */
    static public boolean isLoop(@Nullable PsiElement expression) {
        return null != expression &&
            (expression instanceof ForeachStatement || expression instanceof For || expression instanceof While || expression instanceof DoWhile);
    }
}
