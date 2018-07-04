package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessIgnoreCaseModifierCheckStrategy {
    private static final String message = "'i' modifier is ambiguous here (no alphabet characters in given pattern).";

    static public void apply(final String modifiers, final String pattern, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(modifiers) && !StringUtils.isEmpty(pattern)) {
            if (-1 != modifiers.indexOf('i') && !pattern.matches(".*\\p{L}.*")) {
                holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
