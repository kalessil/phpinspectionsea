package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Recognize (A+)* pattern.
 * See details here: http://www.rexegg.com/regex-explosive-quantifiers.html#compound
 *
 * Whenever you see that a quantifier applies to a token that is already quantified, as in (A+)*,
 * there is potential for the number of steps to explode.
 * Often, the "compounding quantifier" pattern happens when the outside quantifier applies to an alternation,
 * as in (?:\D+|0(?!1))*. Unless you pay attention, you can miss that the (\D+…)* constitutes an explosive quantifier.
 *
 * The lesson here is that when a quantifier needs to apply to another quantifier, we need to prevent the engine
 * from backtracking. We achieve this either by:
 * - making the outer quantifier possessive, e.g. (?:\D+|0(?!1))*+ or
 * - enclosing the expression in an atomic group, e.g. (?>(?:\D+|0(?!1))*)
 */

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class QuantifierCompoundsQuantifierCheckStrategy {
    private static final String messagePattern = "( %s )%s might be exploited (ReDoS, Regular Expression Denial of Service).";

    final static private Pattern regexGroupsToSkip;
    final static private Pattern regexOuterGroup;
    static {
        // Original regex: 	([^\\])(\([^()]*[^\\]\))([^+*])
        regexGroupsToSkip = Pattern.compile("([^\\\\])(\\([^()]*[^\\\\]\\))([^+*])");
        // Original regex: 	(^|[^>])\(([^()]+)\)([+*])([^+]|$)
        regexOuterGroup   = Pattern.compile("(^|[^>])\\(([^()]+)\\)([+*])([^+]|$)");
    }

    static public void apply(@NotNull String pattern, @NotNull StringLiteralExpression target, @NotNull ProblemsHolder holder) {
        if (!pattern.isEmpty()) {
            /* get rid of un-captured groups markers */
            String normalizedPattern = pattern.replaceAll("\\(\\?:", "(");
            /* get rid of nested groups */
            while (regexGroupsToSkip.matcher(normalizedPattern).find()) {
                final Matcher matcher = regexGroupsToSkip.matcher(normalizedPattern);
                if (matcher.find()) {
                    final String fragment = matcher.group(0);
                    if (fragment != null) {
                        normalizedPattern = normalizedPattern.replace(fragment, matcher.group(1) + matcher.group(3));
                    }
                }
            }
            final Matcher matcher = regexOuterGroup.matcher(normalizedPattern);
            while (matcher.find()) {
                final String fragment = matcher.group(2);
                if (fragment != null) {
                    for (final String candidate : fragment.split("\\|")) {
                        if (!candidate.isEmpty() && candidate.matches("^\\\\[dDwWsS][*+]$")) {
                            holder.registerProblem(
                                    target,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), candidate, matcher.group(3)),
                                    ProblemHighlightType.GENERIC_ERROR
                            );
                            break;
                        }
                    }
                }
            }
        }
    }
}
