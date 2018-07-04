package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
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

public class SuspiciousCharactersRangeSpecificationStrategy {
    private static final String messagePattern = "Did you mean [...A-Za-z...] instead of [...%s...]?";

    final static private Pattern regexGreedyCharacterSet;
    static {
        // Original regex: \[([^\[\]]+)\]
        regexGreedyCharacterSet = Pattern.compile("\\[([^\\[\\]]+)\\]");
    }

    static public void apply(final String pattern, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(pattern) && pattern.indexOf('[') >= 0) {
            final Matcher regexMatcher = regexGreedyCharacterSet.matcher(pattern);
            while (regexMatcher.find()) {
                final String set = regexMatcher.group(1);

                final String message;
                if (set.contains("A-z")) {
                    message = String.format(messagePattern, "A-z");
                } else if (set.contains("a-Z")) {
                    message = String.format(messagePattern, "a-Z");
                } else {
                    message = null;
                }

                if (message != null) {
                    holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
    }
}