package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class AllowedModifierCheckStrategy {
    private static final String strProblemDescription = "Unknown modifier '%m%'.";

    static public void apply(final String modifiers, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (!StringUtils.isEmpty(modifiers)) {
            for (char modifier : modifiers.toCharArray()) {
                if (-1 == "eimsuxADJSUX".indexOf(modifier)) {
                    String strError = strProblemDescription.replace("%m%", String.valueOf(modifier));
                    holder.registerProblem(target, strError, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
    }
}
