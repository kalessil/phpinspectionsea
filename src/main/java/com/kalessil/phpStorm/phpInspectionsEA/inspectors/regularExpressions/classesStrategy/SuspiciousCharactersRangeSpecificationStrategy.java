package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy;

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

public class SuspiciousCharactersRangeSpecificationStrategy {
    private static final String messagePattern = "'%s' range in '%s' is looking rather suspicious, please check.";

    final static private Pattern matchGroups;
    final static private Pattern matchRanges;
    static {
        /* original regex: (?:\[(^?(?:[^\\\[\]]|\\.)+)\]) */
        matchGroups = Pattern.compile("(?:\\[(^?(?:[^\\\\\\[\\]]|\\\\.)+)\\])");
        /* original regex: .\-. */
        matchRanges = Pattern.compile(".-.");
    }

    static public void apply(final String pattern, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (pattern != null && !pattern.isEmpty() && pattern.indexOf('[') != -1) {
            final Matcher groupsMatcher = matchGroups.matcher(pattern);
            while (groupsMatcher.find()) {
                final String group          = groupsMatcher.group(1);
                final Matcher rangesMatcher = matchRanges.matcher(group);
                while (rangesMatcher.matches()) {
                    final String range = rangesMatcher.group(0);
                    if (!range.equals("a-z") && !range.equals("A-Z") && !range.equals("0-9")) {
                        holder.registerProblem(
                                target,
                                String.format(messagePattern, range, group),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                    }
                }
            }
        }
    }
}