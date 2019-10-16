package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class DeprecatedModifiersCheckStrategy {
    private static final String message = "'e' modifier is deprecated, please use 'preg_replace_callback()' instead.";

    static public void apply(final String modifiers, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(modifiers) && modifiers.indexOf('e') >= 0) {
            holder.registerProblem(
                    target,
                    ReportingUtil.wrapReportedMessage(message),
                    ProblemHighlightType.GENERIC_ERROR
            );
        }
    }
}
