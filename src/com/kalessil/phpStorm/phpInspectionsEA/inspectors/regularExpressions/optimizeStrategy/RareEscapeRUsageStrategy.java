package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class RareEscapeRUsageStrategy {
    private static final String strProblemDescription = "\\R is not widely known, using a non-unicode alternative, e.g. (?\\r\\n|\\n|\\r) would be more popular";

    static public void apply(final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (pattern.length() > 0 && pattern.indexOf('R') != -1) {
            int count = StringUtils.countMatches(pattern, "\\R") - StringUtils.countMatches(pattern, "\\\\R");
            if (count > 0) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
