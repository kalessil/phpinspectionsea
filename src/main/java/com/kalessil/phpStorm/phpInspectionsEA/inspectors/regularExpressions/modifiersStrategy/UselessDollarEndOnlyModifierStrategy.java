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

public class UselessDollarEndOnlyModifierStrategy {
    private static final String messageAmbiguous = "'D' modifier is ambiguous here (no $ in given pattern).";
    private static final String messageIgnored   = "'D' modifier will be ignored because of 'm'.";

    static public void apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull StringLiteralExpression target,
            @NotNull ProblemsHolder holder
    ) {
        if (modifiers != null && !modifiers.isEmpty() && modifiers.indexOf('D') != -1) {
            if (modifiers.indexOf('m') != -1) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(messageIgnored),
                        ProblemHighlightType.WEAK_WARNING
                );
            }

            if (pattern != null && !pattern.isEmpty()) {
                int countEnds = StringUtils.countMatches(pattern, "$") - StringUtils.countMatches(pattern, "\\$");
                if (countEnds == 0) {
                    holder.registerProblem(
                            target,
                            MessagesPresentationUtil.prefixWithEa(messageAmbiguous),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
            }
        }
    }
}
