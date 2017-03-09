package com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

final public class InterfacesExtractUtil {
    @NotNull
    public static HashSet<PhpClass> getCrawlCompleteInheritanceTree(@NotNull PhpClass objClass, boolean withClasses) {
        final HashSet<PhpClass> processedItems = new HashSet<>();

        if (objClass.isInterface()) {
            processInterface(objClass, processedItems);
        } else {
            processClass(objClass, processedItems, withClasses);
        }

        return processedItems;
    }

    private static void processClass(PhpClass clazz, Set<PhpClass> processedItems, boolean withClasses) {
        if (clazz.isInterface()) {
            throw new InvalidParameterException("Interface should not be provided");
        }

        if (withClasses) {
            processedItems.add(clazz);
        }

        /* re-delegate interface handling */
        for (PhpClass interfacee : clazz.getImplementedInterfaces()) {
            processInterface(interfacee, processedItems);
        }

        /* handle parent class */
        if (null != clazz.getSuperClass()) {
            processClass(clazz.getSuperClass(), processedItems, withClasses);
        }
    }

    private static void processInterface(PhpClass clazz, Set<PhpClass> processedItems) {
        if (!clazz.isInterface()) {
            throw new InvalidParameterException("Class should not be provided");
        }

        if (processedItems.add(clazz)) {
            for (PhpClass objParentInterface : clazz.getImplementedInterfaces()) {
                processInterface(objParentInterface, processedItems);
            }
        }
    }
}
