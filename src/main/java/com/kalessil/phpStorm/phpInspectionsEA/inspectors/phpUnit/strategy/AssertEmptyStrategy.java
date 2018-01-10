package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertEmptyStrategy {
    final static private Map<String, String> targetMethodMapping = new HashMap<>();
    static {
        targetMethodMapping.put("assertTrue",     "assertEmpty");
        targetMethodMapping.put("assertNotFalse", "assertEmpty");
        targetMethodMapping.put("assertFalse",    "assertNotEmpty");
        targetMethodMapping.put("assertNotTrue",  "assertNotEmpty");
    }

    private final static String messagePattern = "'%s(...)' should be used instead.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMethodMapping.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 0 && assertionArguments[0] instanceof PhpEmpty) {
                final PsiElement[] emptyArguments = ((PhpEmpty) assertionArguments[0]).getVariables();
                if (emptyArguments.length == 1) {
                    /* generate QF arguments */
                    final String suggestedAssertion   = targetMethodMapping.get(methodName);
                    final String[] suggestedArguments = new String[assertionArguments.length];
                    suggestedArguments[0] = emptyArguments[0].getText();
                    if (assertionArguments.length > 1) {
                        suggestedArguments[1] = assertionArguments[1].getText();
                    }
                    /* register an issue */
                    holder.registerProblem(
                            reference,
                            String.format(messagePattern, suggestedAssertion),
                            new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                    );
                    result = true;
                }
            }
        }
        return result;
    }
}
