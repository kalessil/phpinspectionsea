package com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

final public class ThrowsResolveUtil {
    static public boolean resolveThrownExceptions(
        @NotNull Method method,
        @NotNull Collection<PhpClass> exceptionsRegistry
    ) {
        final Set<Method> processedMethods = new HashSet<>(); /* SOE was reported, hence the this was introduced */
        final boolean result               = collectThrownAndInherited(method, exceptionsRegistry, processedMethods);
        processedMethods.clear();
        return result;
    }

    static private boolean collectThrownAndInherited(
        @NotNull Method method,
        @NotNull Collection<PhpClass> exceptionsRegistry,
        @NotNull Collection<Method> processedMethods
    ) {
        processedMethods.add(method);
        boolean result                  = false;
        final PhpDocComment annotations = method.getDocComment();
        if (annotations != null) {
            /* find all @throws and remember FQNs, @throws can be combined with @inheritdoc */
            for (final PhpDocReturnTag candidate : PsiTreeUtil.findChildrenOfType(annotations, PhpDocReturnTag.class)) {
                if (candidate.getName().equalsIgnoreCase("@throws")) {
                    /* definition styles can differ: single tags, pipe concatenated or combined  */
                    for (final PhpDocType type : PsiTreeUtil.findChildrenOfType(candidate, PhpDocType.class)) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(type);
                        if (resolved instanceof PhpClass) {
                            exceptionsRegistry.add((PhpClass) resolved);
                        }
                    }
                }
            }
            /* resolve inherit doc tags */
            if (annotations.hasInheritDocTag()) {
                collectInherited(method, exceptionsRegistry, processedMethods);
            }
            result = true;
        }
        return result;
    }

    private static void collectInherited(
        @NotNull Method method,
        @NotNull Collection<PhpClass> exceptionsRegistry,
        @NotNull Collection<Method> processedMethods
    ) {
        final PhpClass clazz = method.getContainingClass();
        if (clazz != null) {
            final String methodName = method.getName();
            /* inherited methods */
            final PhpClass parent = clazz.getSuperClass();
            if (parent != null) {
                final Method parentMethod = parent.findMethodByName(methodName);
                if (parentMethod != null && !processedMethods.contains(parentMethod)) {
                    collectThrownAndInherited(parentMethod, exceptionsRegistry, processedMethods);
                }
            }
            /* contract methods */
            for (final PhpClass implementedInterface : clazz.getImplementedInterfaces()) {
                final Method requiredMethod = implementedInterface.findMethodByName(methodName);
                if (requiredMethod != null && !processedMethods.contains(requiredMethod)) {
                    collectThrownAndInherited(requiredMethod, exceptionsRegistry, processedMethods);
                }
            }
        }
    }
}
