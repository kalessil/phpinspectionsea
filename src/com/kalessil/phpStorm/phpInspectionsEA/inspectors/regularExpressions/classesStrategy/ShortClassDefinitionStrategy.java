package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ShortClassDefinitionStrategy {
    private static final String strProblemDescription = "'%p%' can be replaced with '%r%' (%h%)";

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<String, String>();

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

        return mapping;
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(pattern)) {
            final boolean isUnicodeMode = !StringUtil.isEmpty(modifiers) && modifiers.indexOf('u') != -1;
            final String strHint = isUnicodeMode ? "risky, will match extended sets due to /u" : "safe in non-unicode mode";

            final String patternAdapted = pattern
                    .replace("a-zA-Z",    "A-Za-z")
                    .replace("0-9A-Za-z", "A-Za-z0-9");

            final HashMap<String, String> mapping = getMapping();
            for (String wildcard : mapping.keySet()) {
                if (patternAdapted.contains(wildcard)) {
                    final String message = strProblemDescription
                            .replace("%p%", wildcard)
                            .replace("%r%", mapping.get(wildcard))
                            .replace("%h%", strHint);
                    holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
                }
            }

            //TODO: handle [0-9,] and similar cases when classes are part of allowed/escaped sets
        }
    }
}
