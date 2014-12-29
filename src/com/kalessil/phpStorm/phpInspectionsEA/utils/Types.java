package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;

public class Types {
    final static public String strArray    = "array";
    final static public String strString   = "string";
    final static public String strBoolean  = "bool";
    final static public String strInteger  = "int";
    final static public String strFloat    = "float";
    final static public String strNull     = "null";
    final static public String strVoid     = "void";
    final static public String strMixed    = "mixed";
    final static public String strCallable = "callable";
    final static public String strResource = "resource";

    final static public String strResolvingAbortedOnPsiLevel = "\\aborted-on-psi-level";
    final static public String strClassNotResolved= "\\class-not-resolved";
    //final static public String strNotProcessed = "\\not-processed";

    @NotNull
    public static String getType (@NotNull String strGivenType) {

        String strGivenTypeLowerCase = strGivenType.toLowerCase();

        if (
            strGivenTypeLowerCase.equals(strArray) || strGivenTypeLowerCase.equals("\\array") ||
            strGivenTypeLowerCase.contains("[]"))
        {
            return strArray;
        }

        if (strGivenTypeLowerCase.equals(strString) || strGivenTypeLowerCase.equals("\\string")) {
            return strString;
        }

        if (
            strGivenTypeLowerCase.equals(strBoolean) || strGivenTypeLowerCase.equals("\\bool") ||
            strGivenTypeLowerCase.equals("boolean") || strGivenTypeLowerCase.equals("\\boolean")
        ) {
            return strBoolean;
        }

        if (
            strGivenTypeLowerCase.equals(strInteger) || strGivenTypeLowerCase.equals("\\int") ||
            strGivenTypeLowerCase.equals("integer") || strGivenTypeLowerCase.equals("\\integer")
        ) {
            return strInteger;
        }

        if (strGivenTypeLowerCase.equals(strFloat) || strGivenTypeLowerCase.equals("\\float")) {
            return strFloat;
        }

        if (strGivenTypeLowerCase.equals(strNull) || strGivenTypeLowerCase.equals("\\null")) {
            return strNull;
        }

        if (strGivenTypeLowerCase.equals(strVoid) || strGivenTypeLowerCase.equals("\\void")) {
            return strVoid;
        }

        if (strGivenTypeLowerCase.equals(strMixed) || strGivenTypeLowerCase.equals("\\mixed")) {
            return strMixed;
        }

        if (strGivenTypeLowerCase.equals(strCallable) || strGivenTypeLowerCase.equals("\\callable")) {
            return strCallable;
        }

        if (strGivenTypeLowerCase.equals(strResource) || strGivenTypeLowerCase.equals("\\resource")) {
            return strResource;
        }

        return strGivenType;
    }
}
