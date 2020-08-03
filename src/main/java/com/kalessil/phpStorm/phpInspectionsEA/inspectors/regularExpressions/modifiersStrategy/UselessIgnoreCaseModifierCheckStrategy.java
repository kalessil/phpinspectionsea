package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UselessIgnoreCaseModifierCheckStrategy {
    private static final String message = "'i' modifier is ambiguous here (no alphabet characters in given pattern).";

    private static final Pattern matcher;
    static {
        matcher = Pattern.compile(".*\\p{L}.*", Pattern.DOTALL);
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (modifiers != null && !modifiers.isEmpty() && modifiers.indexOf('i') != -1) {
            final boolean check = pattern != null && !pattern.isEmpty();
            if (check && !matcher.matcher(pattern.replaceAll("\\\\[\\\\dDwWsS]", "")).matches()) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(message),
                        ProblemHighlightType.WEAK_WARNING
                );
            }
        }
    }
}
