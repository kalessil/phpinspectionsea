package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitVersion;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang3.StringUtils;
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
        targetFunctionMapping.put("is_iterable", "iterable");

        targetMapping.put("assertTrue",     "assertInternalType");
        targetMapping.put("assertNotFalse", "assertInternalType");
        targetMapping.put("assertFalse",    "assertNotInternalType");
        targetMapping.put("assertNotTrue",  "assertNotInternalType");
    }

    private final static String messagePatternInternalType = "'%s('%s', ...)' would fit more here.";
    private final static String messagePattern             = "'%s(...)' would fit more here.";

    static public boolean apply(
            @NotNull String methodName,
            @NotNull MethodReference reference,
            @NotNull ProblemsHolder holder,
            @NotNull PhpUnitVersion version
    ) {
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
                        final String suggestedAssertion;
                        final String suggestedType;
                        final String[] suggestedArguments;
                        final String message;
                        if (version.below(PhpUnitVersion.PHPUNIT80)) {
                            suggestedAssertion    = targetMapping.get(methodName);
                            suggestedType         = targetFunctionMapping.get(functionName);
                            suggestedArguments    = new String[assertionArguments.length + 1];
                            suggestedArguments[0] = String.format("'%s'", suggestedType);
                            suggestedArguments[1] = functionArguments[0].getText();
                            if (assertionArguments.length > 1) {
                                suggestedArguments[2] = assertionArguments[1].getText();
                            }
                            message               = String.format(messagePatternInternalType, suggestedAssertion, suggestedType);
                        } else {
                            /* internal type assertions were deprecated */
                            suggestedAssertion = String.format(
                                    targetMapping.get(methodName).replace("InternalType", "Is%s").replace("NotIs", "IsNot"),
                                    StringUtils.capitalize(targetFunctionMapping.get(functionName))
                            );
                            suggestedType         = null;
                            suggestedArguments    = new String[assertionArguments.length];
                            suggestedArguments[0] = functionArguments[0].getText();
                            if (assertionArguments.length > 1) {
                                suggestedArguments[1] = assertionArguments[1].getText();
                            }
                            message               = String.format(messagePattern, suggestedAssertion);

                        }
                        /* register an issue */
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(message),
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
