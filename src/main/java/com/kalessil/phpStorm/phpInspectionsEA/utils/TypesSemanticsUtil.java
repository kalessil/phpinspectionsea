package com.kalessil.phpStorm.phpInspectionsEA.utils;

import java.util.HashSet;

final public class TypesSemanticsUtil {

    /** check if nullable object interfaces */
    public static boolean isNullableObjectInterface(HashSet<String> resolvedTypesSet) {
        int intCountTypesToInspect = resolvedTypesSet.size();
        if (resolvedTypesSet.contains(Types.strClassNotResolved)) {
            --intCountTypesToInspect;
        }
        if (resolvedTypesSet.contains(Types.strNull)) {
            --intCountTypesToInspect;
        }
        /** ensure we still have variants left */
        if (intCountTypesToInspect == 0) {
            return false;
        }

        /** work through types, ensure it's null or classes references */
        for (String strTypeToInspect : resolvedTypesSet) {
            /** skip core types, but null */
            if (strTypeToInspect.charAt(0) != '\\' && !strTypeToInspect.equals(Types.strNull)) {
                return false;
            }
        }

        return true;
    }
}
