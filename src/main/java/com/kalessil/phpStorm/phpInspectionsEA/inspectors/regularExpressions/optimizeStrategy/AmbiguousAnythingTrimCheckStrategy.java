package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AmbiguousAnythingTrimCheckStrategy {
    private static final String messageLeading  = "Leading .* can probably be removed.";
    private static final String messageTrailing = "Trailing .* can probably be removed.";

    static public void apply(
            @NotNull String functionName,
            @NotNull FunctionReference reference,
            @Nullable String pattern,
            @NotNull StringLiteralExpression target,
            @NotNull ProblemsHolder holder
    ) {
        if (pattern != null && !pattern.isEmpty() && functionName.startsWith("preg_match") && reference.getParameters().length == 2) {
            int countBackRefs = StringUtils.countMatches(pattern, "\\0") - StringUtils.countMatches(pattern, "\\\\0");
            if (countBackRefs <= 0) {
                if (pattern.startsWith(".*")) {
                    holder.registerProblem(
                            target,
                            MessagesPresentationUtil.prefixWithEa(messageLeading),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
                if (pattern.endsWith(".*")) {
                    holder.registerProblem(
                            target,
                            MessagesPresentationUtil.prefixWithEa(messageTrailing),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
            }
        }
    }
}
