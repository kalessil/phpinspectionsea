package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
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

final public class UselessDotAllModifierCheckStrategy {
    private static final String message = "'s' modifier is ambiguous here ('.' is missing in the given pattern).";

    static public void apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull  StringLiteralExpression target,
            @NotNull  ProblemsHolder holder
    ) {
        if (modifiers != null && pattern != null && !pattern.isEmpty() && modifiers.indexOf('s') != -1) {
            final String normalized = pattern.replaceAll("\\\\[\\.\\[\\]]", "")
                                             .replaceAll("\\[[^]]+\\]", "");
            if (StringUtils.countMatches(normalized, ".") - StringUtils.countMatches(normalized, "\\.") == 0) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(message),
                        ProblemHighlightType.WEAK_WARNING
                );
            }
        }
    }
}
