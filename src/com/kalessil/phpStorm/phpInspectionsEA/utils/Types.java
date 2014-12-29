package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Types {
    final static public String strArray = "array";
    final static public String strString = "string";
    final static public String strBoolean = "bool";
    final static public String strInteger = "int";
    final static public String strFloat = "float";
    final static public String strNull = "null";

    final static public String strResolvingAbortedOnPsiLevel = "\\aborted-on-psi-level";
    //final static public String strNotProcessed = "\\not-processed";

    @Nullable
    public static String getType (@NotNull String strGivenType) {
        strGivenType = strGivenType.toLowerCase();

        if (strGivenType.equals(strArray) || strGivenType.equals("\\array")) {
            return strArray;
        }

        if (strGivenType.equals(strString) || strGivenType.equals("\\string")) {
            return strString;
        }

        if (
            strGivenType.equals(strBoolean) || strGivenType.equals("\\bool") ||
            strGivenType.equals("boolean") || strGivenType.equals("\\boolean")
        ) {
            return strBoolean;
        }

        if (
            strGivenType.equals(strInteger) || strGivenType.equals("\\int") ||
            strGivenType.equals("integer") || strGivenType.equals("\\integer")
        ) {
            return strInteger;
        }

        if (strGivenType.equals(strFloat) || strGivenType.equals("\\float")) {
            return strFloat;
        }

        if (strGivenType.equals(strNull) || strGivenType.equals("\\null")) {
            return strNull;
        }

        return strGivenType;
    }
}
