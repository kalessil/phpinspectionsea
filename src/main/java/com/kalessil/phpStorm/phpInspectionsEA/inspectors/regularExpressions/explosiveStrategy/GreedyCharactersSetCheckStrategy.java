package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreedyCharactersSetCheckStrategy {
    private static final String messagePattern = "[%e%] is 'greedy'. Please remove %c% as it's a subset of %p%";

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexGreedyCharacterSet = null;
    static {
        // Original regex: \[([^\[\]]+)\]
        regexGreedyCharacterSet = Pattern.compile("\\[([^\\[\\]]+)\\]");
    }

    static public void apply(final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(pattern) && pattern.indexOf('[') >= 0) {
            final Matcher regexMatcher = regexGreedyCharacterSet.matcher(pattern);
            while (regexMatcher.find()) {
                final String set = regexMatcher.group(1);
                String message   = null;

                if (set.contains("\\w") && set.contains("\\d")) {
                   message = messagePattern.replace("%e%", set).replace("%c%", "\\d").replace("%p%", "\\w");
                }
                if (null == message && set.contains("\\W") && set.contains("\\D")) {
                   message = messagePattern.replace("%e%", set).replace("%c%", "\\D").replace("%p%", "\\W");
                }

                if (null != message) {
                    holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
    }
}
