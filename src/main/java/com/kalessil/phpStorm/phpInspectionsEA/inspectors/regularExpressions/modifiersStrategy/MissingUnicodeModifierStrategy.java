package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MissingUnicodeModifierStrategy {
    private static final String messageCharacters = "/u modifier is missing (unicode characters found).";
    private static final String messageCodepoints = "/u modifier is missing (unicode codepoints found).";

    final static private Pattern unicodeCharactersPattern;
    final static private Pattern unicodeCodepointsPattern;
    static {
        // Original regex: .*[^\u0000-\u007F]+.*
        unicodeCharactersPattern = Pattern.compile(".*[^\\u0000-\\u007F]+.*");
        // Original regex: .*\\[pPX].*
        unicodeCodepointsPattern = Pattern.compile(".*\\\\[pPX].*");
    }

    static public void apply(
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull PsiElement target,
            @NotNull  ProblemsHolder holder
    ) {
        if ((modifiers == null || modifiers.indexOf('u') == -1) && !StringUtils.isEmpty(pattern)) {
            if (unicodeCharactersPattern.matcher(pattern).matches()) {
                holder.registerProblem(target, messageCharacters, ProblemHighlightType.GENERIC_ERROR);
            } else {
                final String normalized = StringUtils.replace(pattern, "\\\\", "");
                if (unicodeCodepointsPattern.matcher(normalized).matches()) {
                    holder.registerProblem(target, messageCodepoints, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
    }
}
