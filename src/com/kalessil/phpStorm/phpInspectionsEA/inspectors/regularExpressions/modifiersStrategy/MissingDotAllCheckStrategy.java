package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MissingDotAllCheckStrategy {
    private static final String strProblemDescription = "/s modifier is probably missing (nested tags are not recognized)";

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexTagContentPattern = null;
    static {
        regexTagContentPattern = Pattern.compile(".*>\\.(\\*|\\+)\\?<.*");
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (
            (StringUtil.isEmpty(modifiers) || modifiers.indexOf('s') == -1) &&
            !StringUtil.isEmpty(pattern) && pattern.indexOf('?') != -1
        ) {
            Matcher regexMatcher = regexTagContentPattern.matcher(pattern);
            if (regexMatcher.matches()) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
