package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NestedCallsStrategy {
    private static final String message = "Null pointer exception may occur here (result can be null).";

    private static final Set<String> objectTypes = new HashSet<>();
    static {
        objectTypes.add(Types.strSelf);
        objectTypes.add(Types.strStatic);
        objectTypes.add(Types.strObject);
    }

    public static void apply(@NotNull Function function, @NotNull ProblemsHolder holder) {
        final Project project = holder.getProject();
        for (final FunctionReference reference : PsiTreeUtil.findChildrenOfType(function, FunctionReference.class)) {
            for (final PsiElement argument : reference.getParameters()) {
                if (PhpLanguageUtil.isNull(argument)) {
                    holder.registerProblem(argument, message);
                } else if (argument instanceof MethodReference) {
                    final PhpType resolvedTypes = OpenapiResolveUtil.resolveType((MethodReference) argument, project);
                    if (resolvedTypes != null) {
                        final Set<String> types = resolvedTypes.filterUnknown().getTypes().stream()
                                .map(Types::getType)
                                .collect(Collectors.toSet());
                        if (types.contains(Types.strNull) || types.contains(Types.strVoid)) {
                            types.remove(Types.strNull);
                            types.remove(Types.strVoid);
                            if (types.stream().filter(t -> !t.startsWith("\\") && !objectTypes.contains(t)).count() == 0) {
                                holder.registerProblem(argument, message);
                            }
                        }
                    }
                }
            }
        }
    }
}
