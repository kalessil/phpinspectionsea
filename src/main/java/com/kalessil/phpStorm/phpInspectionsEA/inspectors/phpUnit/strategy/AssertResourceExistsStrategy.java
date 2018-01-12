package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
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

public class AssertResourceExistsStrategy {
    private final static String messagePattern = "'%s(...)' should be used instead.";

    private final static Map<String, String> targetFunctions  = new HashMap<>();
    private final static Map<String, String> targetAssertions = new HashMap<>();
    static {
        targetFunctions.put("file_exists", "File");
        targetFunctions.put("is_dir",      "Directory");

        targetAssertions.put("assertTrue",     "assert%sExists");
        targetAssertions.put("assertNotFalse", "assert%sExists");
        targetAssertions.put("assertFalse",    "assert%sNotExists");
        targetAssertions.put("assertNotTrue",  "assert%sNotExists");
    }

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetAssertions.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 0 && OpenapiTypesUtil.isFunctionReference(assertionArguments[0])) {
                final FunctionReference candidate = (FunctionReference) assertionArguments[0];
                final String functionName         = candidate.getName();
                if (functionName != null && targetFunctions.containsKey(functionName)) {
                    final PsiElement[] functionArguments = candidate.getParameters();
                    if (functionArguments.length == 1) {
                        final String suggestedAssertion = String.format(
                                targetAssertions.get(methodName),
                                targetFunctions.get(functionName)
                        );
                        final String[] suggestedArguments = new String[assertionArguments.length];
                        suggestedArguments[0] = functionArguments[0].getText();
                        if (assertionArguments.length > 1) {
                            suggestedArguments[1] = assertionArguments[1].getText();
                        }
                        holder.registerProblem(
                                reference,
                                String.format(messagePattern, suggestedAssertion),
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
