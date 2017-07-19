package com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

final public class InterfacesExtractUtil {
    @NotNull
    public static HashSet<PhpClass> getCrawlInheritanceTree(@NotNull PhpClass clazz, boolean withClasses) {
        final HashSet<PhpClass> processedItems = new HashSet<>();

        if (clazz.isInterface()) {
            processInterface(clazz, processedItems);
        } else {
            processClass(clazz, processedItems, withClasses);
        }

        return processedItems;
    }

    private static void processClass(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processedItems, boolean withClasses) {
        if (clazz.isInterface()) {
            throw new InvalidParameterException("Interface should not be provided");
        }

        if (withClasses) {
            processedItems.add(clazz);
        }

        /* re-delegate interface handling */
        for (final PhpClass interfacee : clazz.getImplementedInterfaces()) {
            processInterface(interfacee, processedItems);
        }

        /* handle parent class */
        if (clazz.getSuperClass() != null) {
            processClass(clazz.getSuperClass(), processedItems, withClasses);
        }
    }

    private static void processInterface(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processedItems) {
        if (!clazz.isInterface()) {
            throw new InvalidParameterException("Class should not be provided");
        }

        if (processedItems.add(clazz)) {
            for (final PhpClass parentInterface : clazz.getImplementedInterfaces()) {
                processInterface(parentInterface, processedItems);
            }
        }
    }
}
