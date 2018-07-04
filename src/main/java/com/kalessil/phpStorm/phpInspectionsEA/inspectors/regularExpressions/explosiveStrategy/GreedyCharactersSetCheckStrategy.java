package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
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

final public class GreedyCharactersSetCheckStrategy {
    private static final String messagePattern = "[%s] is 'greedy'. Please remove %s as it's a subset of %s.";

    final static private Pattern regexGreedyCharacterSet;
    static {
        // Original regex: \[([^\[\]]+)\]
        regexGreedyCharacterSet = Pattern.compile("\\[([^\\[\\]]+)\\]");
    }

    static public void apply(@NotNull String pattern, @NotNull PsiElement target, @NotNull ProblemsHolder holder) {
        if (!pattern.isEmpty() && pattern.indexOf('[') >= 0) {
            final Matcher regexMatcher = regexGreedyCharacterSet.matcher(pattern);
            while (regexMatcher.find()) {
                final String set = regexMatcher.group(1);
                if (set.contains("\\w") && set.contains("\\d")) {
                    holder.registerProblem(
                            target,
                            String.format(messagePattern, set, "\\d", "\\w"),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                } else if (set.contains("\\W") && set.contains("\\D")) {
                    holder.registerProblem(
                            target,
                            String.format(messagePattern, set, "\\D", "\\W"),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                }
            }
        }
    }
}
