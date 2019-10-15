package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

final public class AssertInternalTypeStrategy {
    final static private Map<String, String> targetFunctionMapping = new HashMap<>();
    final static private Map<String, String> targetMapping         = new HashMap<>();
    static {
        targetFunctionMapping.put("is_array",    "array");
        targetFunctionMapping.put("is_bool",     "bool");
        targetFunctionMapping.put("is_float",    "float");
        targetFunctionMapping.put("is_int",      "int");
        targetFunctionMapping.put("is_null",     "null");
        targetFunctionMapping.put("is_numeric",  "numeric");
        targetFunctionMapping.put("is_object",   "object");
        targetFunctionMapping.put("is_resource", "resource");
        targetFunctionMapping.put("is_string",   "string");
        targetFunctionMapping.put("is_scalar",   "scalar");
        targetFunctionMapping.put("is_callable", "callable");

        targetMapping.put("assertTrue",     "assertInternalType");
        targetMapping.put("assertNotFalse", "assertInternalType");
        targetMapping.put("assertFalse",    "assertNotInternalType");
        targetMapping.put("assertNotTrue",  "assertNotInternalType");
    }

    private final static String messagePattern = "'%s('%s', ...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMapping.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 0 && OpenapiTypesUtil.isFunctionReference(assertionArguments[0])) {
                final FunctionReference functionReference = (FunctionReference) assertionArguments[0];
                final String functionName                 = functionReference.getName();
                if (functionName != null && targetFunctionMapping.containsKey(functionName)) {
                    final PsiElement[] functionArguments = functionReference.getParameters();
                    if (functionArguments.length > 0) {
                        /* generate QF arguments */
                        final String suggestedAssertion   = targetMapping.get(methodName);
                        final String suggestedType        = targetFunctionMapping.get(functionName);
                        final String[] suggestedArguments = new String[assertionArguments.length + 1];
                        suggestedArguments[0] = String.format("'%s'", suggestedType);
                        suggestedArguments[1] = functionArguments[0].getText();
                        if (assertionArguments.length > 1) {
                            suggestedArguments[2] = assertionArguments[1].getText();
                        }
                        /* register an issue */
                        holder.registerProblem(
                                reference,
                                String.format(ReportingUtil.wrapReportedMessage(messagePattern), suggestedAssertion, suggestedType),
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
