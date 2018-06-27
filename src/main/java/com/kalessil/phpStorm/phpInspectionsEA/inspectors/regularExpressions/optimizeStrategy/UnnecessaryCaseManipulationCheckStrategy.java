package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

final public class UnnecessaryCaseManipulationCheckStrategy {
    private static final String messageUnnecessary = "Unnecessary case manipulation (the regex is case-insensitive).";
    private static final String messageNotOptimal  = "Unnecessary case manipulation (use i-flag in regex for better performance).";

    private static final Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("strtolower");
        targetFunctions.add("strtoupper");
        targetFunctions.add("mb_strtolower");
        targetFunctions.add("mb_strtoupper");
    }

    static public void apply(
            @NotNull final String functionName,
            @NotNull final FunctionReference reference,
            @Nullable final String modifiers,
            @NotNull final ProblemsHolder holder
    ) {
        if (functionName.equals("preg_match")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 2 && OpenapiTypesUtil.isFunctionReference(arguments[1])) {
                final String argumentName = ((FunctionReference) arguments[1]).getName();
                if (argumentName != null && targetFunctions.contains(argumentName)) {
                    final boolean isCaseInsensitive = modifiers != null && modifiers.indexOf('i') != -1;
                    holder.registerProblem(arguments[1], isCaseInsensitive ? messageUnnecessary : messageNotOptimal);
                }
            }
        }
    }
}
