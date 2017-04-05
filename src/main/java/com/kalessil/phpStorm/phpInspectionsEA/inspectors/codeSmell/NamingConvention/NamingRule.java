package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;


import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class NamingRule {

    final public static String TYPE_INTERFACE = "interface";
    final public static String TYPE_TRAIT = "trait";
    final public static String TYPE_FINAL = "final";
    final public static String TYPE_ABSTRACT = "abstract";
    final public static String TYPE_CLASS = "class";


    final private String nameRegex;
    final private String type;

    @Nullable
    private final String extendFqn;

    public NamingRule(String nameRegex, String type, @Nullable String extendFqn) {
        this.nameRegex = nameRegex;
        this.type = type;
        this.extendFqn = extendFqn;
    }

    public NamingRule(String nameRegex, String type) {
        this.nameRegex = nameRegex;
        this.type = type;
        this.extendFqn = null;
    }

    public String getType() {
        return type;
    }


    boolean isValid(PhpClass object) {
        return object.getName().matches(this.nameRegex);
    }


    boolean isSupported(PhpValidatableClass object) {
        boolean result = false;
        ArrayList<String> objectExtendsFqns = object.getExtendsFQNs();
        int extendsSize = objectExtendsFqns.size();
        if (extendFqn == null) {
            result = (extendsSize == 0);
        } else {
            for (final String extendClassFqn : objectExtendsFqns) {
                if (extendClassFqn.equals(extendFqn)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }


}
