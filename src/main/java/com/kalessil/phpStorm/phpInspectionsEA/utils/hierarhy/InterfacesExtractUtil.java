package com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class InterfacesExtractUtil {
    @NotNull
    public static HashSet<PhpClass> getCrawlInheritanceTree(@NotNull PhpClass clazz, boolean withClasses) {
        final HashSet<PhpClass> processed = new HashSet<>();
        if (clazz.isInterface()) {
            processInterface(clazz, processed);
        } else {
            processClass(clazz, processed, withClasses);
        }
        return processed;
    }

    private static void processClass(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processedItems, boolean withClasses) {
        if (!clazz.isInterface()) {
            if (withClasses && !processedItems.add(clazz)) {
                return;
            }

            /* re-delegate interface and trait handling */
            OpenapiResolveUtil.resolveImplementedInterfaces(clazz).forEach(i -> processInterface(i, processedItems));
            OpenapiResolveUtil.resolveImplementedTraits(clazz).forEach(i -> processTrait(i, processedItems));

            /* handle parent class */
            final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
            if (parent != null && clazz != parent) {
                processClass(parent, processedItems, withClasses);
            }
        }
    }

    private static void processInterface(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processedItems) {
        if (clazz.isInterface() && processedItems.add(clazz)) {
            OpenapiResolveUtil.resolveImplementedInterfaces(clazz).forEach(i -> processInterface(i, processedItems));
        }
    }

    private static void processTrait(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processedItems) {
        if (clazz.isTrait() && processedItems.add(clazz)) {
            OpenapiResolveUtil.resolveImplementedTraits(clazz).forEach(t -> processTrait(t, processedItems));
        }
    }
}
