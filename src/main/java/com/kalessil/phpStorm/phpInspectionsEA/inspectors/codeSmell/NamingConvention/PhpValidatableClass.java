package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;


import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.List;

public class PhpValidatableClass {


    private final PhpClass phpClass;
    private NamingRule.ObjectType type;
    private ArrayList<String> extendsFQNs;

    PhpValidatableClass(PhpClass phpClass) {
        this.phpClass = phpClass;
    }

    public NamingRule.ObjectType getType() {
        if (type == null) {
            if (phpClass.isInterface()) {
                type = NamingRule.ObjectType.INTERFACE;
            } else if (phpClass.isTrait()) {
                type = NamingRule.ObjectType.TRAIT;
            } else if (phpClass.isFinal()) {
                type = NamingRule.ObjectType.FINAL;
            } else if (phpClass.isAbstract()) {
                type = NamingRule.ObjectType.ABSTRACT;
            } else {
                type = NamingRule.ObjectType.CLASS;
            }
        }
        return type;
    }

    /**
     * Get extended class FQN + all FQN of the interfaces that are implemented in the class
     */
    ArrayList<String> getExtendsFQNs() {
        if (extendsFQNs == null) {
            extendsFQNs = new ArrayList<>();

            List<ClassReference> a = phpClass.getExtendsList().getReferenceElements();
            for (ClassReference extendedClassRef : a) {
                extendsFQNs.add(extendedClassRef.getFQN());
            }

            PhpClass[] implementsInterfaces = phpClass.getImplementedInterfaces();
            for (PhpClass implementInterface : implementsInterfaces) {
                extendsFQNs.add(implementInterface.getFQN());
            }
        }
        return extendsFQNs;
    }

}
