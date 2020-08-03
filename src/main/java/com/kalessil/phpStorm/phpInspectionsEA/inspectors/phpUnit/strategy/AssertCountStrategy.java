package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
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

final public class AssertCountStrategy {
    final static private Map<String, String> targetMapping = new HashMap<>();
    static {
        targetMapping.put("assertSame",      "assertCount");
        targetMapping.put("assertEquals",    "assertCount");
        targetMapping.put("assertNotSame",   "assertNotCount");
        targetMapping.put("assertNotEquals", "assertNotCount");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMapping.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 1 && OpenapiTypesUtil.isFunctionReference(assertionArguments[1])) {
                final FunctionReference candidate = (FunctionReference) assertionArguments[1];
                final String functionName         = candidate.getName();
                if (functionName != null && functionName.equals("count")) {
                    final PsiElement[] functionArguments = candidate.getParameters();
                    if (functionArguments.length == 1) {
                        final String suggestedAssertion   = targetMapping.get(methodName);
                        final String[] suggestedArguments = new String[assertionArguments.length];
                        suggestedArguments[0] = assertionArguments[0].getText();
                        suggestedArguments[1] = functionArguments[0].getText();
                        if (assertionArguments.length > 2) {
                            suggestedArguments[2] = assertionArguments[2].getText();
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