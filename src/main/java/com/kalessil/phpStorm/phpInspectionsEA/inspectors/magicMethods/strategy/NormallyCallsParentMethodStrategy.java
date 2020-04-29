package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NormallyCallsParentMethodStrategy {
    private static final String messagePattern = "%s is probably missing %s::%s call.";

    static public void apply(@NotNull Method method, @NotNull ProblemsHolder holder) {
        final PhpClass clazz = method.getContainingClass();
        if (clazz != null) {
            final String methodName           = method.getName();
            final PhpClass parentClazz        = OpenapiResolveUtil.resolveSuperClass(clazz);
            final Method   parentMethod       = parentClazz == null ? null : OpenapiResolveUtil.resolveMethod(parentClazz, methodName);
            final PhpClass parentMethodHolder = parentMethod == null ? null : parentMethod.getContainingClass();
            if (parentMethodHolder != null && !parentMethod.isAbstract() && !parentMethod.getAccess().isPrivate()) {
                final boolean isUsed = PsiTreeUtil.findChildrenOfType(method, MethodReference.class).stream()
                        .anyMatch(call -> methodName.equals(call.getName()));
                if (!isUsed) {
                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                    if (nameNode != null) {
                        holder.registerProblem(
                                nameNode,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), methodName, parentMethodHolder.getName(), methodName)
                        );
                    }

                }
            }
        }
    }
}
