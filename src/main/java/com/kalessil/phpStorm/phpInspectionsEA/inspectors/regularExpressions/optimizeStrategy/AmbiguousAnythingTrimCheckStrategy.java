package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

final public class AmbiguousAnythingTrimCheckStrategy {
    private static final String strProblemLeading  = "Leading .* can be removed.";
    private static final String strProblemTrailing = "Trailing .* can be removed.";

    static public void apply(
            final String functionName,
            @NotNull final FunctionReference reference,
            final String pattern,
            @NotNull final PsiElement target,
            @NotNull final ProblemsHolder holder
    ) {
        if (
            2 == reference.getParameters().length &&
            !StringUtils.isEmpty(pattern) &&
            !StringUtils.isEmpty(functionName) && functionName.startsWith("preg_match")
        ) {
            int countBackRefs = StringUtils.countMatches(pattern, "\\0") - StringUtils.countMatches(pattern, "\\\\0");
            if (countBackRefs > 0) {
                return;
            }

            if (pattern.startsWith(".*")) {
                holder.registerProblem(target, strProblemLeading, ProblemHighlightType.WEAK_WARNING);
            }
            if (pattern.endsWith(".*")) {
                holder.registerProblem(target, strProblemTrailing, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
