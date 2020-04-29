package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

final public class SequentialClassesCollapseCheckStrategy {
    private static final String messagePattern = "'%s' can be replaced with '%s{...}'.";

    final static private Pattern regexRepeatedClasses;
    static {
        regexRepeatedClasses = Pattern.compile("((\\[([^\\]]+)\\])(\\*|\\+|\\?|\\{[^\\}]+\\})?\\2(\\*|\\+|\\?|\\{[^\\}]+\\})?)+");
    }

    static public void apply(@Nullable String pattern, @NotNull StringLiteralExpression target, @NotNull ProblemsHolder holder) {
        if (pattern != null && !pattern.isEmpty() && pattern.indexOf('[') >= 0) {
            final Matcher matcher = regexRepeatedClasses.matcher(pattern);
            if (matcher.find()) {
                holder.registerProblem(
                        target,
                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), matcher.group(0), matcher.group(2)),
                        ProblemHighlightType.WEAK_WARNING
                );
            }
        }
    }
}
