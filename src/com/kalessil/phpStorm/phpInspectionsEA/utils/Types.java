package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
    final static public String strStatic   = "static";
    final static public String strSelf     = "self";
    final static public String strObject   = "object";

    final static public String strResolvingAbortedOnPsiLevel = "\\aborted-on-psi-level";
    final static public String strClassNotResolved= "\\class-not-resolved";
    //final static public String strNotProcessed = "\\not-processed";

    static private HashMap<String, String> mapTypes = null;
    static private HashMap<String, String> getTypesMap () {
        if (null == mapTypes) {
            mapTypes = new HashMap<String, String>();

            mapTypes.put(strArray,     strArray);
            mapTypes.put("\\array",    strArray);

            mapTypes.put(strString,    strString);
            mapTypes.put("\\string",   strString);

            mapTypes.put(strBoolean,   strBoolean);
            mapTypes.put("\\bool",     strBoolean);
            mapTypes.put("boolean",    strBoolean);
            mapTypes.put("\\boolean",  strBoolean);

            mapTypes.put(strInteger,   strInteger);
            mapTypes.put("\\int",      strInteger);
            mapTypes.put("integer",    strInteger);
            mapTypes.put("\\integer",  strInteger);

            mapTypes.put(strFloat,     strFloat);
            mapTypes.put("\\float",    strFloat);

            mapTypes.put(strNull,      strNull);
            mapTypes.put("\\null",     strNull);

            mapTypes.put(strVoid,      strVoid);
            mapTypes.put("\\void",     strVoid);

            mapTypes.put(strMixed,     strMixed);
            mapTypes.put("\\mixed",    strMixed);

            mapTypes.put(strCallable,  strCallable);
            mapTypes.put("\\callable", strCallable);
            mapTypes.put("\\closure",  strCallable);

            mapTypes.put(strResource,  strResource);
            mapTypes.put("\\resource", strResource);

            mapTypes.put(strStatic,    strStatic);
            mapTypes.put("\\static",   strStatic);
            mapTypes.put("$this",      strStatic);

            mapTypes.put(strSelf,      strSelf);
            mapTypes.put("\\self",     strSelf);

            mapTypes.put(strObject,    strObject);
            mapTypes.put("\\object",   strObject);
        }

        return mapTypes;
    }

    public static String getType (@NotNull String strGivenType) {
        /* special case: array definition */
        if (strGivenType.contains("[]")) {
            return strArray;
        }

        HashMap<String, String> mapping = getTypesMap();
        String strResolvedType = mapping.get(strGivenType.toLowerCase());
        if (null != strResolvedType) {
            return strResolvedType;
        }

        return strGivenType;
    }
}
