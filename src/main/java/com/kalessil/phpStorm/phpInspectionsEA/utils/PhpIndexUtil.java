package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final public class PhpIndexUtil {

    static public LinkedList<PhpClass> getObjectInterfaces(@NotNull String strName, @NotNull PhpIndex objIndex, boolean strict) {
        final List<PhpClass> collection = new ArrayList<>();

        collection.addAll(objIndex.getClassesByFQN(strName));
        collection.addAll(objIndex.getInterfacesByFQN(strName));

        if (!strict && collection.isEmpty()) {
            collection.addAll(objIndex.getClassesByName(strName));
            collection.addAll(objIndex.getInterfacesByName(strName));
        }

        return new LinkedList<>(collection);
    }
}
