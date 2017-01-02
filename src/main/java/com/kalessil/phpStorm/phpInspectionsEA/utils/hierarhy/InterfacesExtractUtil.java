package com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.HashSet;

final public class InterfacesExtractUtil {
    @NotNull
    public static HashSet<PhpClass> getCrawlCompleteInheritanceTree(@NotNull PhpClass objClass, boolean withClasses) {
        HashSet<PhpClass> processedItems = new HashSet<>();

        if (objClass.isInterface()) {
            processInterface(objClass, processedItems, withClasses);
        } else {
            processClass(objClass, processedItems, withClasses);
        }

        return processedItems;
    }

    private static void processClass(PhpClass objClass, HashSet<PhpClass> processedItems, boolean withClasses) {
        if (objClass.isInterface()) {
            throw new InvalidParameterException("Interface should not be provided");
        }

        if (withClasses) {
            processedItems.add(objClass);
        }

        /* re-delegate interface handling */
        for (PhpClass objInterface : objClass.getImplementedInterfaces()) {
            processInterface(objInterface, processedItems, withClasses);
        }

        /* handle parent class */
        if (null != objClass.getSuperClass()) {
            processClass(objClass.getSuperClass(), processedItems, withClasses);
        }
    }

    private static void processInterface(PhpClass objClass, HashSet<PhpClass> processedItems, boolean withClasses) {
        if (!objClass.isInterface()) {
            throw new InvalidParameterException("Class should not be provided");
        }

        if (processedItems.add(objClass)) {
            for (PhpClass objParentInterface : objClass.getImplementedInterfaces()) {
                processInterface(objParentInterface, processedItems, withClasses);
            }
        }
    }
}
