package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;

import java.util.Collection;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

public enum BinaryExpressionUtil {
    ;

    private static final Collection<IElementType> operationTypes = new HashSet<>();

    static {
        operationTypes.add(PhpTokenTypes.opEQUAL);
        operationTypes.add(PhpTokenTypes.opIDENTICAL);
        operationTypes.add(PhpTokenTypes.opNOT_EQUAL);
        operationTypes.add(PhpTokenTypes.opNOT_IDENTICAL);
        operationTypes.add(PhpTokenTypes.opGREATER);
        operationTypes.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        operationTypes.add(PhpTokenTypes.opLESS);
        operationTypes.add(PhpTokenTypes.opLESS_OR_EQUAL);
        operationTypes.add(PhpTokenTypes.kwINSTANCEOF);
    }

    public static boolean isComparison(@NotNull final BinaryExpression binaryExpression) {
        return operationTypes.contains(binaryExpression.getOperationType());
    }
}
