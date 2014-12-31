package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.LinkedList;

public class PhpIndexUtil {

    static public LinkedList<PhpClass> getObjectInterfaces(String strName, PhpIndex objIndex) {
        LinkedList<PhpClass> collection = new LinkedList<>();

        collection.addAll(objIndex.getClassesByFQN(strName));
        collection.addAll(objIndex.getInterfacesByFQN(strName));

        if (collection.size() == 0) {
            collection.addAll(objIndex.getClassesByName(strName));
            collection.addAll(objIndex.getInterfacesByName(strName));
        }

        return collection;
    }
}
