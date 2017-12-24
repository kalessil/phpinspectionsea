package com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
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
    static public boolean resolveThrownExceptions(@NotNull Method method, @NotNull Collection<PhpClass> exceptionsRegistry) {
        final Set<Method> processedMethods = new HashSet<>(); /* SOE was reported, hence the this was introduced */
        final boolean result               = collectThrownAndInherited(method, exceptionsRegistry, processedMethods, true);
        processedMethods.clear();
        return result;
    }

    static private boolean collectThrownAndInherited(
        @NotNull Method method,
        @NotNull Collection<PhpClass> exceptionsRegistry,
        @NotNull Collection<Method> processedMethods,
        boolean lookupWorkflow
    ) {
        processedMethods.add(method);
        boolean result                  = false;
        final PhpDocComment annotations = method.getDocComment();
        if (annotations == null) {
            /* if PhpDoc is missing, check workflow; but we'll search only `throw new ...` statements */
            if (lookupWorkflow && !method.isAbstract()) {
                for (final PhpThrow thrown : PsiTreeUtil.findChildrenOfType(method, PhpThrow.class)) {
                    final PsiElement argument = thrown.getArgument();
                    if (argument instanceof NewExpression) {
                        final PsiElement classReference = ((NewExpression) argument).getClassReference();
                        if (classReference != null) {
                            /* false-positives: lambdas and anonymous classes */
                            if (PsiTreeUtil.getParentOfType(thrown, Method.class) != method) {
                                continue;
                            }
                            /* false-positives: ALL try-enclosed throw statements */
                            else if (PsiTreeUtil.getParentOfType(thrown, Try.class, false, (Class) Method.class) != null) {
                                continue;
                            }

                            final PsiElement clazz = OpenapiResolveUtil.resolveReference((PsiReference) classReference);
                            if (clazz instanceof PhpClass) {
                                exceptionsRegistry.add((PhpClass) clazz);
                            }
                        }
                    }
                }
                result = true;
            }
        } else {
            /* find all @throws and remember FQNs, @throws can be combined with @inheritdoc */
            for (final PhpDocTag candidate : PsiTreeUtil.findChildrenOfType(annotations, PhpDocTag.class)) {
                if (candidate.getName().equalsIgnoreCase("@throws")) {
                    /* definition styles can differ: single tags, pipe concatenated or combined  */
                    for (final PhpDocType type : PsiTreeUtil.findChildrenOfType(candidate, PhpDocType.class)) {
                        final PsiElement clazz = OpenapiResolveUtil.resolveReference(type);
                        if (clazz instanceof PhpClass) {
                            exceptionsRegistry.add((PhpClass) clazz);
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
                    collectThrownAndInherited(parentMethod, exceptionsRegistry, processedMethods, false);
                }
            }
            /* contract methods */
            for (final PhpClass implementedInterface : clazz.getImplementedInterfaces()) {
                final Method requiredMethod = implementedInterface.findMethodByName(methodName);
                if (requiredMethod != null && !processedMethods.contains(requiredMethod)) {
                    collectThrownAndInherited(requiredMethod, exceptionsRegistry, processedMethods, false);
                }
            }
        }
    }
}
