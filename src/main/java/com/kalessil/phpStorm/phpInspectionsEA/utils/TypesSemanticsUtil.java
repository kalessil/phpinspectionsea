package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

final public class TypesSemanticsUtil {

    /** check if nullable object interfaces */
    public static boolean isNullableObjectInterface(@NotNull Set<String> resolvedTypesSet) {
        int intCountTypesToInspect = resolvedTypesSet.size();
        if (resolvedTypesSet.contains(Types.strNull)) {
            --intCountTypesToInspect;
        }
        /* ensure we still have variants left */
        if (intCountTypesToInspect == 0) {
            return false;
        }

        /* work through types, ensure it's null or classes references */
        for (String strTypeToInspect : resolvedTypesSet) {
            /* skip core types, but null */
            if (strTypeToInspect.charAt(0) != '\\' && !strTypeToInspect.equals(Types.strNull)) {
                return false;
            }
        }

        return true;
    }
}
