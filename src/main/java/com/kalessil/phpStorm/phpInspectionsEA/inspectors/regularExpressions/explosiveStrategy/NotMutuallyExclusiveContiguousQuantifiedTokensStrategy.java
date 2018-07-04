package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * See details here: http://www.rexegg.com/regex-explosive-quantifiers.html#contiguous
 *
 * Consider this pattern: ^\d+\w*@
 * The \d and the \w are both able to match digits: they are not mutually exclusive.
 *
 * Against a string such as 123, the pattern must fail. While trying all the possibilities in order to find the match,
 * the engine will let the \d+ give up characters that will be matched by the \w*. Exploring these paths takes time:
 * the engine takes 16 steps to reach failure.
 *
 * Adding one digit to the test string, e.g. 1234, the engine takes 25 steps to fail. With ten digits, it takes 121 steps.
 * With 100 digits, it takes 10,201 steps. The situation is clearly far better than in the first example. The number of steps
 * required to fail in relation to the size of the string does not grow exponentially, but it still explodes—without looking
 * at it closely its complexity seems to be quadratic or thereabouts, i.e. O(n2). It takes 1,100 digits to reach a million
 * steps. That's a lot more than many subject strings but a lot less than others—that's only a page-and-a-half of average text.
 *
 * The lesson here is to try to use contiguous tokens that are mutually exclusive, following the rule of
 * contrast (http://www.rexegg.com/regex-style.html#contrast) from the regex style guide.
 */

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NotMutuallyExclusiveContiguousQuantifiedTokensStrategy {
    private static final String messagePattern = "%s and %s are not mutually exclusive in '%s' which can be exploited (ReDoS, Regular Expression Denial of Service).";

    final static private Pattern regexSequentialQuantifiedTokens;
    static {
        // Original regex: (?:\\[dDwW](?:[*+])){2,}
        regexSequentialQuantifiedTokens = Pattern.compile("(?:\\\\[dDwW](?:[*+])){2,}");
    }

    static public boolean apply(@NotNull String pattern, @NotNull PsiElement target, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (!pattern.isEmpty()) {
            final Matcher regexMatcher = regexSequentialQuantifiedTokens.matcher(pattern);
            while (regexMatcher.find()) {
                final String fragment = regexMatcher.group(0);
                if (fragment != null) {
                    final String normalized = fragment.replaceAll("[*+]", "");
                    if (normalized.contains("\\w\\d") || normalized.contains("\\d\\w")) {
                        holder.registerProblem(
                                target,
                                String.format(messagePattern, "\\d", "\\w", fragment),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                        result = true;
                    } else if (normalized.contains("\\W\\D") || normalized.contains("\\D\\W")) {
                        holder.registerProblem(
                                target,
                                String.format(messagePattern, "\\D", "\\W", fragment),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                        result = true;
                    }
                }
            }
        }
        return result;
    }
}
