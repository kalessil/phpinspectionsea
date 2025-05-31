package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
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
            @NotNull String functionName,
            @Nullable String modifiers,
            @Nullable String pattern,
            @NotNull  StringLiteralExpression target,
            @NotNull  ProblemsHolder holder
    ) {
        if ((modifiers == null || modifiers.indexOf('u') == -1) && pattern != null && ! pattern.isEmpty() && ! functionName.equals("preg_quote")) {
            if (unicodeCharactersPattern.matcher(pattern).matches()) {
                holder.registerProblem(
                        target,
                        MessagesPresentationUtil.prefixWithEa(messageCharacters),
                        ProblemHighlightType.GENERIC_ERROR
                );
            } else {
                final String normalized = StringUtils.replace(pattern, "\\\\", "");
                if (unicodeCodepointsPattern.matcher(normalized).matches()) {
                    holder.registerProblem(
                            target,
                            MessagesPresentationUtil.prefixWithEa(messageCodepoints),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                }
            }
        }
    }
}
