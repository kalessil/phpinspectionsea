package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class MissingDotAllCheckStrategy {
    private static final String message = "/s modifier is probably missing (nested tags are not recognized).";

    final static private Pattern regexTagContentPattern;
    static {
        regexTagContentPattern = Pattern.compile(".*>\\.([*+])\\?<.*");
    }

    static public boolean apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull PsiElement target,
            @NotNull ProblemsHolder holder
    ) {
        boolean result = false;
        if (pattern != null && pattern.indexOf('?') != -1) {
            final boolean hasModifier = modifiers != null && modifiers.indexOf('s') != -1;
            if (!hasModifier) {
                final Matcher regexMatcher = regexTagContentPattern.matcher(pattern);
                if (result = regexMatcher.matches()) {
                    holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        }
        return result;
    }
}
