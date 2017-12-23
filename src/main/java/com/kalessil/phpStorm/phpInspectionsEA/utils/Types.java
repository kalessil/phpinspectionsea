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
    final static public String strNumber   = "number";
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

    final static private Map<String, String> mapping = new HashMap<>();
    static  {
        mapping.put(strArray,     strArray);
        mapping.put("\\array",    strArray);

        mapping.put(strIterable,  strIterable);
        mapping.put("\\iterable", strIterable);

        mapping.put(strString,    strString);
        mapping.put("\\string",   strString);

        mapping.put(strBoolean,   strBoolean);
        mapping.put("\\bool",     strBoolean);
        mapping.put("boolean",    strBoolean);
        mapping.put("\\boolean",  strBoolean);
        mapping.put("false",      strBoolean);
        mapping.put("\\false",    strBoolean);
        mapping.put("true",       strBoolean);
        mapping.put("\\true",     strBoolean);

        mapping.put(strInteger,   strInteger);
        mapping.put("\\int",      strInteger);
        mapping.put("integer",    strInteger);
        mapping.put("\\integer",  strInteger);

        mapping.put(strFloat,     strFloat);
        mapping.put("\\float",    strFloat);

        mapping.put(strNumber,    strNumber);
        mapping.put("\\number",   strNumber);

        mapping.put(strNull,      strNull);
        mapping.put("\\null",     strNull);

        mapping.put(strVoid,      strVoid);
        mapping.put("\\void",     strVoid);

        mapping.put(strMixed,     strMixed);
        mapping.put("\\mixed",    strMixed);

        mapping.put(strCallable,  strCallable);
        mapping.put("\\callable", strCallable);
        mapping.put("\\closure",  strCallable);

        mapping.put(strResource,  strResource);
        mapping.put("\\resource", strResource);

        mapping.put(strStatic,    strStatic);
        mapping.put("\\static",   strStatic);
        mapping.put("$this",      strStatic);

        mapping.put(strSelf,      strSelf);
        mapping.put("\\self",     strSelf);

        mapping.put(strObject,    strObject);
        mapping.put("\\object",   strObject);
    }

    @NotNull
    public static String getType(@NotNull String givenType) {
        return givenType.contains("[]") ? strArray : mapping.getOrDefault(givenType.toLowerCase(), givenType);
    }
}
