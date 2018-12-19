package com.kalessil.phpStorm.phpInspectionsEA.utils.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ClassInStringContextStrategy {
    public static boolean apply (
        @Nullable PsiElement nonStringOperand,
        @NotNull ProblemsHolder holder,
        @NotNull PsiElement expression,
        @NotNull String classHasNoToStringMessage
    ) {
        if (null == nonStringOperand) {
            return false;
        }

        final Set<String> resolvedTypes = new HashSet<>();
        if (nonStringOperand instanceof PhpTypedElement) {
            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) nonStringOperand, holder.getProject());
            if (resolved != null) {
                resolved.filterUnknown().getTypes().forEach(t -> resolvedTypes.add(Types.getType(t)));
            }
        }
        if (!TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes)) {
            resolvedTypes.clear();
            return false;
        }

        /* collect classes to check if __toString() is there */
        final PhpIndex index             = PhpIndex.getInstance(holder.getProject());
        final List<PhpClass> listClasses = new ArrayList<>();
        resolvedTypes.stream()
                .filter(fqn  -> fqn.charAt(0) == '\\')
                .forEach(fqn -> listClasses.addAll(OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(fqn, index)));
        resolvedTypes.clear();

        /* check methods, error on first one violated requirements */
        for (final PhpClass clazz : listClasses) {
            if (OpenapiResolveUtil.resolveMethod(clazz, "__toString") == null) {
                holder.registerProblem(
                        expression,
                        classHasNoToStringMessage.replace("%class%", clazz.getFQN()),
                        ProblemHighlightType.ERROR
                );
                break;
            }
        }

        /* terminate inspection, php will call __toString() */
        listClasses.clear();
        return true;
    }
}
