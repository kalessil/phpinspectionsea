package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class UselessIgnoreCaseModifierCheckStrategy {
    private static final String message = "'i' modifier is ambiguous here (no alphabet characters in given pattern)";

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(modifiers) && !StringUtil.isEmpty(pattern)) {
            if (-1 != modifiers.indexOf('i') && !pattern.matches(".*\\p{L}.*")) {
                holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
