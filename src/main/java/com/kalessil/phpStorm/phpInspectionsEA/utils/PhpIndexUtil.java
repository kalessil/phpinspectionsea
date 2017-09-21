package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

final public class PhpIndexUtil {

    static public LinkedList<PhpClass> getObjectInterfaces(@NotNull String strName, @NotNull PhpIndex objIndex, boolean strict) {
        final LinkedList<PhpClass> collection = new LinkedList<>();

        collection.addAll(objIndex.getClassesByFQN(strName));
        collection.addAll(objIndex.getInterfacesByFQN(strName));

        if (!strict && collection.isEmpty()) {
            collection.addAll(objIndex.getClassesByName(strName));
            collection.addAll(objIndex.getInterfacesByName(strName));
        }

        return collection;
    }
}
