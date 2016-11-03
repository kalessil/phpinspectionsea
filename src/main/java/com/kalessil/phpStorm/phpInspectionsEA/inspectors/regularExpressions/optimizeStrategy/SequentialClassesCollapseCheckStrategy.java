package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequentialClassesCollapseCheckStrategy {
    private static final String strProblemDescription = "'%p%' can be replaced with '%r%{...}'";

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexRepeatedClasses = null;
    static {
        regexRepeatedClasses = Pattern.compile("((\\[([^\\]]+)\\])(\\*|\\+|\\?|\\{[^\\}]+\\})?\\2(\\*|\\+|\\?|\\{[^\\}]+\\})?)+");
    }

    static public void apply(final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(pattern) && pattern.indexOf('[') >= 0) {
            Matcher regexMatcher = regexRepeatedClasses.matcher(pattern);
            if (regexMatcher.find()) {
                String strError = strProblemDescription
                        .replace("%p%", regexMatcher.group(0))
                        .replace("%r%", regexMatcher.group(2));
                holder.registerProblem(target, strError, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
