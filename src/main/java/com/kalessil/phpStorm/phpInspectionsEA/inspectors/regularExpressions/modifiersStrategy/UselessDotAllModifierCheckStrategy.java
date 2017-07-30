package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.apache.commons.lang.StringUtils;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessDotAllModifierCheckStrategy {
    private static final String strProblemDescription = "'s' modifier is ambiguous here (no . in given pattern).";

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(modifiers) && !StringUtils.isEmpty(pattern) && modifiers.indexOf('s') >= 0) {
            int countDots = StringUtils.countMatches(pattern, ".") - StringUtils.countMatches(pattern, "\\.");
            if (0 == countDots) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
