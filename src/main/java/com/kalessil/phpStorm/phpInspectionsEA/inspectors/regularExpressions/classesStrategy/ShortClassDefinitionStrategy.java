package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ShortClassDefinitionStrategy {
    private static final String messagePattern = "'%p%' can be replaced with '%r%' (%h%).";

    private static final HashMap<String, String> mapping = new HashMap<>();
    static {
        mapping.put("[0-9]",         "\\d");
        mapping.put("[:digit:]",     "\\d");

        mapping.put("[^0-9]",        "\\D");
        mapping.put("[^\\d]",        "\\D");

        mapping.put("[:word:]",      "\\w");
        mapping.put("[A-Za-z0-9_]",  "\\w");

        mapping.put("[^\\w]",        "\\W");
        mapping.put("[^A-Za-z0-9_]", "\\W");

        mapping.put("[^\\s]",        "\\S");
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(pattern)) {
            final boolean isUnicodeMode = !StringUtils.isEmpty(modifiers) && modifiers.indexOf('u') != -1;
            final String safetyHint     = isUnicodeMode ? "risky, will match extended sets due to /u" : "safe in non-unicode mode";

            /* normalize only first found cases - sufficient for reporting */
            final String patternAdapted = pattern
                    .replace("a-zA-Z",    "A-Za-z")
                    .replace("0-9A-Za-z", "A-Za-z0-9");

            for (Map.Entry<String, String> replacement : mapping.entrySet()) {
                final String wildcard = replacement.getKey();
                if (patternAdapted.contains(wildcard)) {
                    final String message = messagePattern
                            .replace("%p%", wildcard)
                            .replace("%r%", replacement.getValue())
                            .replace("%h%", safetyHint);

                    holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
                }
            }

            //TODO: handle [0-9,] and similar cases when classes are part of allowed/escaped sets
        }
    }
}
