package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertSameStrategy {
    private final static Map<String, String> targetMapping = new HashMap<>();
    static {
        targetMapping.put("assertNotEquals", "assertNotSame");
        targetMapping.put("assertEquals",    "assertSame");
    }

    private final static String messagePattern = "This check is type-unsafe, consider using '%s(...)' instead.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 1 && isPrimitiveScalar(holder.getProject(), arguments[1]) && isPrimitiveScalar(holder.getProject(), arguments[0])) {
                final String suggestedAssertion   = targetMapping.get(methodName);
                final String[] suggestedArguments = new String[arguments.length];
                Arrays.stream(arguments).map(PsiElement::getText).collect(Collectors.toList()).toArray(suggestedArguments);
                holder.registerProblem(
                        reference,
                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                        new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments));

                result = true;
            }
        }
        return result;
    }

    static private boolean isPrimitiveScalar(@NotNull Project project, @NotNull PsiElement expression) {
        boolean result = false;
        if (expression instanceof PhpTypedElement) {
            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, project);
            if (resolved != null && !resolved.hasUnknown()) {
                result = resolved.getTypes().stream().noneMatch(type -> {
                    final String normalizedType = Types.getType(type);
                    return normalizedType.startsWith("\\") || normalizedType.equals(Types.strArray);
                });
            }
        }
        return result;
    }
}
