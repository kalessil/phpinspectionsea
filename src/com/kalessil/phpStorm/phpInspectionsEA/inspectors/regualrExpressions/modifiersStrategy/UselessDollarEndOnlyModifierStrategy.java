package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessDollarEndOnlyModifierStrategy {
    private static final String strProblemDescription = "'D' modifier is ambiguous here (no $ in given pattern)";
    private static final String strProblemIgnored = "'D' modifier will ignored because of 'm'";

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(modifiers) && !StringUtil.isEmpty(pattern) && modifiers.indexOf('D') >= 0) {
            if (modifiers.indexOf('m') >= 0) {
                holder.registerProblem(target, strProblemIgnored, ProblemHighlightType.WEAK_WARNING);
                return;
            }

            int countEnds = StringUtils.countMatches(pattern, "$") - StringUtils.countMatches(pattern, "\\$");
            if (0 == countEnds) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
