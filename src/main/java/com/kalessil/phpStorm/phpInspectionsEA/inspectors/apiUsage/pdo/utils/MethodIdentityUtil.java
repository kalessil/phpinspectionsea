package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MethodIdentityUtil {
    static public boolean isReferencingMethod(
            @Nullable MethodReference reference,
            @NotNull String classFqn,
            @NotNull String methodName
    ) {
        boolean result = false;

        final String referenceName = (null == reference ? null : reference.getName());
        if (referenceName != null && referenceName.equals(methodName)) {
            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
            if (resolved instanceof Method) {
                final Method method = (Method) resolved;
                result              = method.getFQN().equals(classFqn + "::" + methodName);
                if (!result) {
                    final PhpClass clazz = method.getContainingClass();
                    if (clazz != null && !clazz.isTrait()) {
                        final Set<PhpClass> parents = InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true);
                        for (final PhpClass parent : parents) {
                            if (parent.getFQN().equals(classFqn)) {
                                result = true;
                                break;
                            }
                        }
                        parents.clear();
                    }
                }
            }
        }

        return result;
    }
}
