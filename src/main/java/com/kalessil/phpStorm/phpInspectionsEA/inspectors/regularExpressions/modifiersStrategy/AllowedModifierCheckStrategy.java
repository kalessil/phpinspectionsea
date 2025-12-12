package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

final public class AllowedModifierCheckStrategy {
    private static final String message = "Unknown modifier '%s'.";

    private static final String modifiersSince56 = "eimsuxADJSUX";
    private static final String modifiersSince82 = "eimsuxADJSUXn";

    static public void apply(@NotNull String functionName, @Nullable String modifiers, @NotNull PsiElement target, @NotNull ProblemsHolder holder) {
        if (modifiers != null && ! modifiers.isEmpty() && ! functionName.equals("preg_quote")) {
            final String allowedModifiers = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP820) ? modifiersSince82 : modifiersSince56;
            for (final char modifier : modifiers.toCharArray()) {
                if (allowedModifiers.indexOf(modifier) == -1) {
                    holder.registerProblem(
                            target,
                            String.format(MessagesPresentationUtil.prefixWithEa(message), modifier),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                }
            }
        }
    }
}
