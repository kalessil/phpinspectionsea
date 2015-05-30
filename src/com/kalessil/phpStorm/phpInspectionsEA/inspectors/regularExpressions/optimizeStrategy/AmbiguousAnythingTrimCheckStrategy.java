package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class AmbiguousAnythingTrimCheckStrategy {
    private static final String strProblemLeading = "Leading .* can be removed";
    private static final String strProblemTrailing = "Trailing .* can be removed";

    static public void apply(final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(pattern)) {
            int countBackRefs = StringUtils.countMatches(pattern, "\\0") - StringUtils.countMatches(pattern, "\\\\0");
            if (countBackRefs > 0) {
                return;
            }

            /* allow preg_match_* only if no  match-container were provided */

            if (pattern.startsWith(".*")) {
                holder.registerProblem(target, strProblemLeading, ProblemHighlightType.WEAK_WARNING);
            }
            if (pattern.endsWith(".*")) {
                holder.registerProblem(target, strProblemTrailing, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
