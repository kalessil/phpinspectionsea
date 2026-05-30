package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

final public class MissingCaselessRestrictStrategy {
    private static final String messagePattern = "/%s modifier is missing (r modifier found).";

    static public void apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull StringLiteralExpression target,
            @NotNull final ProblemsHolder holder
    ) {
        if (modifiers != null && modifiers.indexOf('r') != -1) {
            if (modifiers.indexOf('i') == -1) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "i")),
                        ProblemHighlightType.GENERIC_ERROR
                );
            }
            if (modifiers.indexOf('u') == -1) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "u")),
                        ProblemHighlightType.GENERIC_ERROR
                );
            }
        }
    }
}
