package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final public class Types {
    final static public String strArray    = "array";
    final static public String strIterable = "iterable";
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
    final static public String strEmptySet = "ø";

    final static public String strResolvingAbortedOnPsiLevel = "\\aborted-on-psi-level";
    final static public String strClassNotResolved           = "\\class-not-resolved";

    static private Map<String, String> mapTypes = null;
    static private Map<String, String> getTypesMap () {
        if (null == mapTypes) {
            mapTypes = new HashMap<>();

            mapTypes.put(strArray,     strArray);
            mapTypes.put("\\array",    strArray);

            mapTypes.put(strIterable,  strIterable);
            mapTypes.put("\\iterable", strIterable);

            mapTypes.put(strString,    strString);
            mapTypes.put("\\string",   strString);

            mapTypes.put(strBoolean,   strBoolean);
            mapTypes.put("\\bool",     strBoolean);
            mapTypes.put("boolean",    strBoolean);
            mapTypes.put("\\boolean",  strBoolean);
            mapTypes.put("false",      strBoolean);
            mapTypes.put("\\false",    strBoolean);
            mapTypes.put("true",       strBoolean);
            mapTypes.put("\\true",     strBoolean);

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

    public static String getType (@NotNull String givenType) {
        /* special case: array definition */
        if (givenType.contains("[]")) {
            return strArray;
        }

        final String resolvedType = getTypesMap().get(givenType.toLowerCase());
        if (null != resolvedType) {
            return resolvedType;
        }

        return givenType;
    }
}
