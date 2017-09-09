package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class OpenapiTypesUtil {
    static public boolean isLambda(@Nullable PsiElement expression) {
        if (is(expression, PhpElementTypes.CLOSURE)) {
            expression = expression.getFirstChild();
        }
        return expression instanceof Function && ((Function) expression).isClosure();
    }

    static public boolean isAssignment(@Nullable PsiElement expression) {
        return expression != null && expression.getNode().getElementType() == PhpElementTypes.ASSIGNMENT_EXPRESSION;
    }

    static public boolean isFunctionReference(@Nullable PsiElement expression) {
        return expression != null && expression.getNode().getElementType() == PhpElementTypes.FUNCTION_CALL;
    }

    static public boolean isLoop(@Nullable PsiElement expression) {
        return null != expression &&
            (expression instanceof ForeachStatement || expression instanceof For || expression instanceof While || expression instanceof DoWhile);
    }

    static public boolean isStatementImpl(@Nullable PsiElement expression) {
        return expression != null && expression.getNode().getElementType() == PhpElementTypes.STATEMENT;
    }

    static public boolean is(@Nullable PsiElement expression, @NotNull IElementType type) {
        return expression != null && expression.getNode().getElementType() == type;
    }
}
