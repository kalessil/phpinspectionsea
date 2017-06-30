package com.kalessil.phpStorm.phpInspectionsEA.utils.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

        final PhpIndex index = PhpIndex.getInstance(holder.getProject());
        final Function scope = ExpressionSemanticUtil.getScope(nonStringOperand);

        final HashSet<String> resolvedTypes = new HashSet<>();
        TypeFromPsiResolvingUtil.resolveExpressionType(nonStringOperand, scope, index, resolvedTypes);
        if (!TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes)) {
            return false;
        }

        /* collect classes to check if __toString() is there */
        final List<PhpClass> listClasses = new ArrayList<>();
        for (final String classFqn : resolvedTypes) {
            if (classFqn.charAt(0) == '\\') {
                listClasses.addAll(PhpIndexUtil.getObjectInterfaces(classFqn, index, false));
            }
        }

        /* check methods, error on first one violated requirements */
        for (final PhpClass clazz : listClasses) {
            if (clazz.findMethodByName("__toString") == null) {
                final String message = classHasNoToStringMessage.replace("%class%", clazz.getFQN());
                holder.registerProblem(expression, message, ProblemHighlightType.ERROR);

                break;
            }
        }

        /* terminate inspection, php will call __toString() */
        listClasses.clear();
        return true;
    }
}
