package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
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
        return expression instanceof AssignmentExpression && !(expression instanceof SelfAssignmentExpression);
    }
}
