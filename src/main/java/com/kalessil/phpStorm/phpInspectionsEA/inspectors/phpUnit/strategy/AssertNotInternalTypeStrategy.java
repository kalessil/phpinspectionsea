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

final public class AssertNotInternalTypeStrategy {
    final static private Map<String, String> targetMapping = new HashMap<>();
    static {
        targetMapping.put("is_array",    "array");
        targetMapping.put("is_bool",     "bool");
        targetMapping.put("is_float",    "float");
        targetMapping.put("is_int",      "int");
        targetMapping.put("is_null",     "null");
        targetMapping.put("is_numeric",  "numeric");
        targetMapping.put("is_object",   "object");
        targetMapping.put("is_resource", "resource");
        targetMapping.put("is_string",   "string");
        targetMapping.put("is_scalar",   "scalar");
        targetMapping.put("is_callable", "callable");
    }

    private final static String messagePattern = "'assertNotInternalType('%s', ...)' should be used instead.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (methodName.equals("assertFalse") || methodName.equals("assertNotTrue")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 0 && OpenapiTypesUtil.isFunctionReference(arguments[0])) {
                final FunctionReference functionReference = (FunctionReference) arguments[0];
                final String functionName                 = functionReference.getName();
                if (functionName != null && targetMapping.containsKey(functionName)) {
                    final PsiElement[] functionArguments = functionReference.getParameters();
                    if (functionArguments.length > 0) {
                        result = true;
                        holder.registerProblem(reference, String.format(messagePattern, targetMapping.get(functionName)));
                    }
                }
            }
        }
        return result;
    }

}
