package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.LinkedList;

final public class PhpIndexUtil {

    static public LinkedList<PhpClass> getObjectInterfaces(String strName, PhpIndex objIndex, boolean strict) {
        LinkedList<PhpClass> collection = new LinkedList<PhpClass>();

        collection.addAll(objIndex.getClassesByFQN(strName));
        collection.addAll(objIndex.getInterfacesByFQN(strName));

        if (!strict && 0 == collection.size()) {
            collection.addAll(objIndex.getClassesByName(strName));
            collection.addAll(objIndex.getInterfacesByName(strName));
        }

        return collection;
    }
}
