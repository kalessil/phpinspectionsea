package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.apache.commons.lang.StringUtils;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NormallyCallsParentMethodStrategy {
    private static final String messagePattern = "%m% is probably missing %c%::%m% call.";

    static public void apply(final Method method, final ProblemsHolder holder) {
        final String methodName   = method.getName();
        final PhpClass clazz      = method.getContainingClass();
        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
        if (null == nameNode || null == clazz || StringUtils.isEmpty(methodName)) {
            return;
        }

        /* find parent with protected/public method, if not overrides anything, terminate inspection */
        final PhpClass parent       = clazz.getSuperClass();
        final Method   parentMethod = parent == null ? null : parent.findMethodByName(methodName);
        final PhpClass methodHolder = parentMethod == null ? null : parentMethod.getContainingClass();
        if (methodHolder == null || parentMethod.isAbstract() || parentMethod.getAccess().isPrivate()) {
            return;
        }

        /* find all calls inside methods and check if any of them calls parent */
        for (final MethodReference call : PsiTreeUtil.findChildrenOfType(method, MethodReference.class)) {
            final String innerCallName = call.getName();
            if (innerCallName != null && innerCallName.equals(methodName)) {
                return;
            }
        }

        /* report the issue */
        final String message = messagePattern
            .replace("%m%", methodName)
            .replace("%c%", methodHolder.getName());
        holder.registerProblem(nameNode, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
}
