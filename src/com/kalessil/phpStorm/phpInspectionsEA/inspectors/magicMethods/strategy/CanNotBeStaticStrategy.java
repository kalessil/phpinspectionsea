package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.Method;

public class CanNotBeStaticStrategy {
    private static final String strProblemDescription = "%m% cannot be static";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (method.isStatic() && null != method.getNameIdentifier()) {
            String strMessage = strProblemDescription.replace("%m%", method.getName());
            holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.ERROR);
        }
    }
}
