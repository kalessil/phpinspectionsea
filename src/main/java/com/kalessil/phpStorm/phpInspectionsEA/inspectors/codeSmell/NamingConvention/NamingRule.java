package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;


import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NamingRule {
    public enum ObjectType {
        INTERFACE,
        TRAIT,
        FINAL,
        ABSTRACT,
        CLASS,
    }

    @NotNull
    final private String nameRegex;

    @NotNull
    final private ObjectType type;

    @Nullable
    private final String extendFqn;

    public NamingRule(@NotNull String nameRegex, @NotNull ObjectType type, @Nullable String extendFqn) {
        this.nameRegex = nameRegex;
        this.type = type;
        this.extendFqn = extendFqn;
    }

    public NamingRule(@NotNull String nameRegex, @NotNull ObjectType type) {
        this.nameRegex = nameRegex;
        this.type = type;
        this.extendFqn = null;
    }

    @NotNull
    public ObjectType getType() {
        return type;
    }


    boolean isValid(@NotNull PhpClass object) {
        return object.getName().matches(this.nameRegex);
    }


    boolean isSupported(@NotNull  PhpValidatableClass object) {
        boolean result = false;
        final List<String> objectExtendsFqns = object.getExtendsFQNs();
        int extendsSize = objectExtendsFqns.size();
        if (null == extendFqn) {
            result = (0 == extendsSize);
        } else {
            for (String extendClassFqn : objectExtendsFqns) {
                if (extendClassFqn.equals(extendFqn)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }


}
