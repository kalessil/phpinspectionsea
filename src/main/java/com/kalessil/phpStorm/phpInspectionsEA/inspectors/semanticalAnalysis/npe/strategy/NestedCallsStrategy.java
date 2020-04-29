package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
        for (final MethodReference reference : PsiTreeUtil.findChildrenOfType(function, MethodReference.class)) {
            /* identify nullable arguments */
            final Map<Integer, PsiElement> nullableArguments = new TreeMap<>();
            final PsiElement[] arguments                     = reference.getParameters();
            for (int index = 0, argumentsCount = arguments.length; index < argumentsCount; ++index) {
                final PsiElement argument = arguments[index];
                if (argument instanceof MethodReference) {
                    final PhpType resolvedTypes = OpenapiResolveUtil.resolveType((MethodReference) argument, project);
                    if (resolvedTypes != null) {
                        final Set<String> types = resolvedTypes.filterUnknown().getTypes().stream()
                                .map(Types::getType)
                                .collect(Collectors.toSet());
                        if (types.contains(Types.strNull) || types.contains(Types.strVoid)) {
                            types.remove(Types.strNull);
                            types.remove(Types.strVoid);
                            if (types.stream().noneMatch(t -> !t.startsWith("\\") && !objectTypes.contains(t))) {
                                nullableArguments.put(index, argument);
                            }
                        }
                    }
                } else if (PhpLanguageUtil.isNull(argument)) {
                    nullableArguments.put(index, argument);
                }
            }
            /* filter nullable parameters (false-positives) */
            if (!nullableArguments.isEmpty()) {
                final PsiElement resolvedReference = OpenapiResolveUtil.resolveReference(reference);
                if (resolvedReference instanceof Method) {
                    final Parameter[] parameters = ((Method) resolvedReference).getParameters();
                    nullableArguments.forEach((index, argument) -> {
                        if (argument != null && index < parameters.length) {
                            final Parameter parameter  = parameters[index];
                            final PhpType declaredType = OpenapiResolveUtil.resolveDeclaredType(parameter);
                            final boolean canBeNull    =
                                PhpLanguageUtil.isNull(parameter.getDefaultValue()) ||
                                declaredType.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strNull)) ||
                                declaredType.filterPrimitives().isEmpty();
                            if (!canBeNull) {
                                holder.registerProblem(
                                        argument,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
                            }
                        }
                    });
                }
                nullableArguments.clear();
            }
        }
    }
}
