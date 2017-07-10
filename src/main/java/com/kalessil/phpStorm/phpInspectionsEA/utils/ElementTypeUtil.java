package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;

import java.util.HashMap;
import java.util.Map;

public enum ElementTypeUtil {
    ;

    private static final Map<IElementType, IElementType> reverseTypes = new HashMap<>();

    static {
        reverseTypes.put(PhpTokenTypes.opGREATER, PhpTokenTypes.opLESS_OR_EQUAL);
        reverseTypes.put(PhpTokenTypes.opGREATER_OR_EQUAL, PhpTokenTypes.opLESS);
        reverseTypes.put(PhpTokenTypes.opLESS, PhpTokenTypes.opGREATER_OR_EQUAL);
        reverseTypes.put(PhpTokenTypes.opLESS_OR_EQUAL, PhpTokenTypes.opGREATER);
    }

    public static IElementType rotateOperation(final IElementType operation) {
        if (reverseTypes.containsKey(operation)) {
            return reverseTypes.get(operation);
        }

        return operation;
    }
}
