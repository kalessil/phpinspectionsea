package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.MultiassignmentExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
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

final public class SuspiciousArrayDestructuringStrategy {
    private static final String message = "This assignment doesn't make any sense here, as the assigned value isn't an array.";

    static public void apply(@NotNull MultiassignmentExpression expression, @NotNull ProblemsHolder holder) {
        PhpPsiElement value = expression.getValue();
        if (OpenapiTypesUtil.isPhpExpressionImpl(value)) {
            value = value.getFirstPsiChild();
        }
        if (value instanceof PhpTypedElement) {
            final Set<String> resolved = new HashSet<>();
            final PhpType resolvedType = OpenapiResolveUtil.resolveType((PhpTypedElement) value, holder.getProject());
            if (resolvedType != null) {
                resolvedType.filterUnknown().getTypes().forEach(t -> resolved.add(Types.getType(t)));
            }
            if (! resolved.isEmpty()) {
                if (! resolved.contains(Types.strArray)) {
                    holder.registerProblem(
                            expression,
                            String.format(MessagesPresentationUtil.prefixWithEa(message))
                    );
                }
                resolved.clear();
            }
        }
    }
}
