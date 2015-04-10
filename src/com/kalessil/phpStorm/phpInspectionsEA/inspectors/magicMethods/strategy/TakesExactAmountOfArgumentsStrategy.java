package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.Method;

public class TakesExactAmountOfArgumentsStrategy {
    private static final String strProblemDescription = "%m% takes exactly %n% arguments";

    static public void apply(final int argumentsAmount, final Method method, final ProblemsHolder holder) {
        if (argumentsAmount != method.getParameters().length && null != method.getNameIdentifier()) {
            String strMessage = strProblemDescription
                    .replace("%m%", method.getName())
                    .replace("%n%", String.valueOf(argumentsAmount));
            holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.ERROR);
        }
    }
}
