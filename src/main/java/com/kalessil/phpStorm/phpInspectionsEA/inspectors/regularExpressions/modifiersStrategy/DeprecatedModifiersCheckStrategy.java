package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class DeprecatedModifiersCheckStrategy {
    private static final String message = "'e' modifier is deprecated, please use 'preg_replace_callback()' instead.";

    static public void apply(final String modifiers, @NotNull final StringLiteralExpression target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(modifiers) && modifiers.indexOf('e') >= 0) {
            holder.registerProblem(
                    target,
                    MessagesPresentationUtil.prefixWithEa(message),
                    ProblemHighlightType.GENERIC_ERROR
            );
        }
    }
}
