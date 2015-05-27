package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessMultiLineModifierStrategy {
    private static final String strProblemDescription = "'m' modifier is ambiguous here (no ^ or $ in given pattern)";

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(modifiers) && !StringUtil.isEmpty(pattern) && modifiers.indexOf('m') >= 0) {
            int countBegins = StringUtils.countMatches(pattern, "^") - StringUtils.countMatches(pattern, "[^");
            int countEnds   = StringUtils.countMatches(pattern, "$") - StringUtils.countMatches(pattern, "\\$");
            if (0 == countBegins || 0 == countEnds) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}

