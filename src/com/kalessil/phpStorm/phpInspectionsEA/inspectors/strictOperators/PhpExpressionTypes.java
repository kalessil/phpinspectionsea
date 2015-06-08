package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;

public class PhpExpressionTypes {
    private final HashSet<String> types = new HashSet<String>();
    private final PhpIndex objIndex;

    public PhpExpressionTypes(final PsiElement expr, @NotNull final ProblemsHolder holder) {
        objIndex = PhpIndex.getInstance(holder.getProject());

        if (expr != null) {
            final Function objScope = ExpressionSemanticUtil.getScope(expr);
            TypeFromPsiResolvingUtil.resolveExpressionType(expr, objScope, objIndex, types);
        }

        types.remove(Types.strResolvingAbortedOnPsiLevel);
        types.remove(Types.strClassNotResolved);
        if (types.contains(Types.strCallable)) {
            types.add(Types.strString);
        } else if (types.isEmpty()) {
            types.add(Types.strMixed);
        }
    }

    public PhpExpressionTypes(final String strTypes, @NotNull final ProblemsHolder holder) {
        objIndex = PhpIndex.getInstance(holder.getProject());

        if ((strTypes.indexOf('?') >= 0) || (strTypes.indexOf('#') >= 0)) {
            types.add(Types.strMixed);
        } else {
            for (final String str : strTypes.split("\\|")) {
                if (!str.isEmpty()) {
                    types.add(Types.getType(str));
                }
            }
        }

        if (types.contains(Types.strCallable)) {
            types.add(Types.strString);
        } else if (types.isEmpty()) {
            types.add(Types.strMixed);
        }
    }

    public boolean equals(@NotNull final PhpExpressionTypes another) {
        final HashSet<String> copy = new HashSet<String>(types);
        copy.retainAll(another.types);
        return !copy.isEmpty();

    }

    public String toString() {
        switch (types.size()) {
            case 0:
                return "unknown";
            case 1:
                return types.iterator().next();
            default:
                final StringBuilder sb = new StringBuilder();
                for (final String s : types) {
                    sb.append(s);
                    sb.append('|');
                }
                return sb.delete(sb.length() - 1, sb.length()).toString();
        }
    }

    public boolean contains(final String type) {
        return types.contains(type);
    }

    public boolean instanceOf(final PhpExpressionTypes base) {
        final boolean instanceOfObject = base.types.contains("object");
        for (final String type1 : types) {
            if (type1.charAt(0) == '\\') {
                if (instanceOfObject) {
                    return true;
                }

                final HashSet<String> extendslist = new HashSet<String>();
                extendslist.add(type1);
                for (final PhpClass typeclass : objIndex.getAnyByFQN(type1)) {
                    extendslist.addAll(Arrays.asList(typeclass.getInterfaceNames()));
                    extendslist.addAll(Arrays.asList(typeclass.getTraitNames()));
                    extendslist.addAll(Arrays.asList(typeclass.getMixinNames()));
                    for (final PhpClass parentclass : typeclass.getSupers()) {
                        extendslist.add(parentclass.getFQN());
                        extendslist.addAll(Arrays.asList(parentclass.getInterfaceNames()));
                        extendslist.addAll(Arrays.asList(parentclass.getTraitNames()));
                        extendslist.addAll(Arrays.asList(parentclass.getMixinNames()));
                    }
                }

                for (final String type2 : base.types) {
                    if (type2.charAt(0) == '\\') {
                        if (extendslist.contains(type2)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isInt() {
        return types.contains(Types.strInteger);
    }

    public boolean isFloat() {
        return types.contains(Types.strFloat);
    }

    public boolean isNumeric() {
        return types.contains(Types.strInteger) || types.contains(Types.strFloat);
    }

    public boolean isString() {
        return types.contains(Types.strString);
    }

    public boolean isBoolean() {
        return types.contains(Types.strBoolean);
    }

    public boolean isArray() {
        return types.contains(Types.strArray);
    }

    public boolean isNull() {
        return types.contains(Types.strNull);
    }

    public boolean isMixed() {
        return types.contains(Types.strMixed);
    }

    public boolean isObject() {
        for (final String type : types) {
            if (type.charAt(0) == '\\') {
                return true;
            }
        }
        return false;
    }

    public boolean isArrayAccess() {
        for (final String strType : types) {
            for (final PhpClass phpClass : objIndex.getClassesByFQN(strType)) {
                for (final String strInterface : phpClass.getInterfaceNames()) {
                    if (strInterface.equals("\\ArrayAccess")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
