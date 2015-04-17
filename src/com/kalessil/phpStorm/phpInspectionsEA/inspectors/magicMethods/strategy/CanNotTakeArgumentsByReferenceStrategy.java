package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;

public class CanNotTakeArgumentsByReferenceStrategy {
    private static final String strProblemDescription = "%m% cannot take arguments by reference";

    static public void apply(final Method method, final ProblemsHolder holder) {
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isPassByRef() && null != method.getNameIdentifier()){
                String strMessage = strProblemDescription.replace("%m%", method.getName());
                holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.ERROR);

                return;
            }
        }
    }
}
