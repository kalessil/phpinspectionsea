package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

public class AllowedModifierCheckStrategy {
    private static final String message = "Unknown modifier '%s'.";

    static public void apply(@NotNull String functionName, @Nullable String modifiers, @NotNull PsiElement target, @NotNull ProblemsHolder holder) {
        if (modifiers != null && !modifiers.isEmpty() && !functionName.equals("preg_quote")) {
            for (char modifier : modifiers.toCharArray()) {
                if ("eimsuxADJSUX".indexOf(modifier) == -1) {
                    holder.registerProblem(
                            target,
                            String.format(ReportingUtil.wrapReportedMessage(message), String.valueOf(modifier)),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                }
            }
        }
    }
}
