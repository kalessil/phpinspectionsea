package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
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

final public class AssertInternalTypeStrategy {
    final static private Map<String, String> targetFunctionMapping = new HashMap<>();
    final static private Map<String, String> targetMethodMapping    = new HashMap<>();
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

        targetMethodMapping.put("assertTrue",     "assertInternalType");
        targetMethodMapping.put("assertNotFalse", "assertInternalType");
        targetMethodMapping.put("assertFalse",    "assertNotInternalType");
        targetMethodMapping.put("assertNotTrue",  "assertNotInternalType");
    }

    private final static String messagePattern = "'%s('%s', ...)' should be used instead.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMethodMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 0 && OpenapiTypesUtil.isFunctionReference(arguments[0])) {
                final FunctionReference functionReference = (FunctionReference) arguments[0];
                final String functionName                 = functionReference.getName();
                if (functionName != null && targetFunctionMapping.containsKey(functionName)) {
                    final PsiElement[] functionArguments = functionReference.getParameters();
                    if (functionArguments.length > 0) {
                        result = true;
                        holder.registerProblem(
                                reference,
                                String.format(
                                        messagePattern,
                                        targetMethodMapping.get(methodName),
                                        targetFunctionMapping.get(functionName)
                                )
                        );
                    }
                }
            }
        }
        return result;
    }

}
