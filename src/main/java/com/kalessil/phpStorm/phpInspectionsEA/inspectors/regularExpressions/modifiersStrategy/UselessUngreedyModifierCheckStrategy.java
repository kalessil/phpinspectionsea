package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class UselessUngreedyModifierCheckStrategy {
    private static final String message = "'U' modifier is ambiguous here ('*', '+' or '?' are missing in the given pattern).";

    static public void apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull PsiElement target,
            @NotNull ProblemsHolder holder
    ) {
        if (modifiers != null && pattern != null && !pattern.isEmpty() && modifiers.indexOf('U') != -1) {
            final String normalized = pattern.replaceAll("\\[[^\\]]+\\]", "");
            for (final String qualifier : new String[]{"*", "+", "?"}) {
                if (
                    normalized.contains(qualifier) &&
                    StringUtils.countMatches(normalized, qualifier) - StringUtils.countMatches(normalized, "\\" + qualifier) != 0
                ) {
                    return;
                }
            }
            holder.registerProblem(
                    target,
                    ReportingUtil.wrapReportedMessage(message),
                    ProblemHighlightType.WEAK_WARNING
            );
        }
    }
}
