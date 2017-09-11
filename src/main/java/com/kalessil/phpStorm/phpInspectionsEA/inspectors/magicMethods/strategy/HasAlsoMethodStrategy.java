package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.StringUtils;

public class HasAlsoMethodStrategy {
    private static final String strProblemDescription = "%m% should have pair method %p%.";

    static public void apply(final Method method, final String pair, final ProblemsHolder holder) {
        final String methodName = method.getName();
        final PhpClass clazz    = method.getContainingClass();
        if (StringUtils.isEmpty(methodName) || null == clazz || null == method.getNameIdentifier()) {
            return;
        }

        if (null == clazz.findMethodByName(pair)) {
            String strMessage = strProblemDescription
                    .replace("%m%", methodName)
                    .replace("%p%", pair);
            holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.GENERIC_ERROR);
        }
    }
}
