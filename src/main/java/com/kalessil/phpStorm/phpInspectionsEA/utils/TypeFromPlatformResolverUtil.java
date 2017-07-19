package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TypeFromPlatformResolverUtil {
    public static void resolveExpressionType(@NotNull PsiElement expression, @NotNull HashSet<String> types) {
        final Project project = expression.getProject();
        final PhpIndex index  = PhpIndex.getInstance(project);
        final Function scope  = ExpressionSemanticUtil.getScope(expression);

        for (final String resolvedType : ((PhpTypedElement) expression).getType().global(project).getTypes()) {
            final boolean isSignatureProvided = resolvedType.contains("?") || resolvedType.contains("#");
            if (isSignatureProvided) {
                TypeFromPsiResolvingUtil.resolveExpressionType(expression, scope, index, types);
                continue;
            }

            types.add(Types.getType(resolvedType));
        }

        types.remove(Types.strClassNotResolved);
        types.remove(Types.strResolvingAbortedOnPsiLevel);
    }
}
