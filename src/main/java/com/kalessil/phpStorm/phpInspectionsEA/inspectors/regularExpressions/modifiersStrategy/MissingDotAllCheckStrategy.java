package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MissingDotAllCheckStrategy {
    private static final String message = "/s modifier is probably missing (nested tags are not recognized).";

    final static private Pattern regexTagContentPattern;
    static {
        regexTagContentPattern = Pattern.compile(".*>\\.([*+])\\?<.*");
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if ((modifiers == null || modifiers.indexOf('s') == -1) && pattern != null && pattern.indexOf('?') != -1) {
            final Matcher matcher = regexTagContentPattern.matcher(pattern);
            if (matcher.matches()) {
                holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
