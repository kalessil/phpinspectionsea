package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitVersion;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

public class AssertContainsStrategy {
    final static private Map<String, String> targetMapping = new HashMap<>();
    static {
        targetMapping.put("assertTrue",     "assertContains");
        targetMapping.put("assertNotFalse", "assertContains");
        targetMapping.put("assertFalse",    "assertNotContains");
        targetMapping.put("assertNotTrue",  "assertNotContains");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(
            @NotNull String methodName,
            @NotNull MethodReference reference,
            @NotNull ProblemsHolder holder,
            @NotNull PhpUnitVersion version
    ) {
        boolean result = false;
        if (version.below(PhpUnitVersion.PHPUNIT90) && targetMapping.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 0 && OpenapiTypesUtil.isFunctionReference(assertionArguments[0])) {
                final FunctionReference candidate = (FunctionReference) assertionArguments[0];
                final String functionName         = candidate.getName();
                if (functionName != null && functionName.equals("in_array")) {
                    final PsiElement[] functionArguments = candidate.getParameters();
                    if (functionArguments.length >= 2) {
                        final String suggestedAssertion   = targetMapping.get(methodName);
                        final String[] suggestedArguments = new String[assertionArguments.length + 1];
                        suggestedArguments[0] = functionArguments[0].getText();
                        suggestedArguments[1] = functionArguments[1].getText();
                        if (assertionArguments.length > 1) {
                            suggestedArguments[2] = assertionArguments[1].getText();
                        }
                        holder.registerProblem(
                                reference,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                        );

                        result = true;
                    }
                }
            }
        }
        return result;
    }

}
