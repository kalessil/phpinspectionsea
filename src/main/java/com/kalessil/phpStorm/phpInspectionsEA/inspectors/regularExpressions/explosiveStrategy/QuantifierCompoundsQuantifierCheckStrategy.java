package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
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
public class QuantifierCompoundsQuantifierCheckStrategy {
    private static final String messagePattern = "(...%i%...)%o% might be exploited (ReDoS, Regular Expression Denial of Service).";

    final static private Pattern regexMarker;
    static {
        // Original regex: [^\+\*]([\+\*]|\{\d\,\}|\{\d{2,}\}|\{\d,\d{2,}\})([^\+\)]|$)
        regexMarker = Pattern.compile("[^\\+\\*]([\\+\\*]|\\{\\d\\,\\}|\\{\\d{2,}\\}|\\{\\d,\\d{2,}\\})([^\\+\\)]|$)");
    }

    static public void apply(final String pattern, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        final Matcher externalQualifierMatcher = regexMarker.matcher(pattern);
        while (externalQualifierMatcher.find()) {
            final int quantifierStart = externalQualifierMatcher.start(1);
            if (quantifierStart > 0 && pattern.charAt(quantifierStart - 1) == ')') {
                String patternSegment = null;

                /* try extracting inner expression from '(...)<qualifier>' */
                int nestingLevel = 0;
                int cursor       = quantifierStart - 1;
                char character;
                while (cursor >= 0) {
                    character = pattern.charAt(cursor);
                    if (character == ')') {
                        ++nestingLevel;
                        --cursor;
                        continue;
                    }

                    if (character == '(') {
                        --nestingLevel;
                        if (0 == nestingLevel) {
                            patternSegment = pattern.substring(cursor + 1, quantifierStart - 1);
                            break;
                        }
                        --cursor;
                        continue;
                    }

                    --cursor;
                }

                /* extracted,*/
                if (!StringUtils.isEmpty(patternSegment)) {
                    final Matcher internalQualifierMatcher = regexMarker.matcher(patternSegment);
                    if (internalQualifierMatcher.find()) {
                        final String message = messagePattern
                                .replace("%i%", internalQualifierMatcher.group(1))
                                .replace("%o%", externalQualifierMatcher.group(1));
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR);
                        return;
                    }
                }
            }
        }
    }
}
