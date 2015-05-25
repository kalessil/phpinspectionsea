package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class UselessMultiLineModifierStrategy {
    private static final String strProblemDescription = "'m' modifier is ambiguous here (no ^ or $ in given pattern)";

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(modifiers) && !StringUtil.isEmpty(pattern) && modifiers.indexOf('m') >= 0) {
            if (!pattern.startsWith("^") || !pattern.endsWith("$")) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}

