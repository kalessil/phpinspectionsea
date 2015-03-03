package com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy;

import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.security.InvalidParameterException;
import java.util.HashSet;

public class InterfacesExtractUtil {
    public static HashSet<PhpClass> getCrawlCompleteInheritanceTree(PhpClass objClass) {
        HashSet<PhpClass> processedItems = new HashSet<PhpClass>();

        if (objClass.isInterface()) {
            processInterface(objClass, processedItems);
        } else {
            processClass(objClass, processedItems);
        }

        return processedItems;
    }

    private static void processClass(PhpClass objClass, HashSet<PhpClass> processedItems) {
        if (objClass.isInterface()) {
            throw new InvalidParameterException("Interface shall not be provided");
        }

        /** re-delegate interface handling */
        for (PhpClass objInterface : objClass.getImplementedInterfaces()) {
            processInterface(objInterface, processedItems);
        }

        /** handle parent class */
        if (null != objClass.getSuperClass()) {
            processClass(objClass.getSuperClass(), processedItems);
        }
    }

    private static void processInterface(PhpClass objClass, HashSet<PhpClass> processedItems) {
        if (!objClass.isInterface()) {
            throw new InvalidParameterException("Class shall not be provided");
        }

        if (processedItems.add(objClass)) {
            for (PhpClass objParentInterface : objClass.getImplementedInterfaces()) {
                processInterface(objParentInterface, processedItems);
            }
        }
    }
}
