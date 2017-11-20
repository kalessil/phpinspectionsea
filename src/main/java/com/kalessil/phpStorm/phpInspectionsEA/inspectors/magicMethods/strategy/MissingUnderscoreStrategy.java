package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissingUnderscoreStrategy {
    private static final String problemDescription = "%m% is not a magic method. Did you mean _%m%?";

    private static final List<String> invalidNames = Arrays.asList(
            "_construct",
            "_destruct",
            "_call",
            "_callStatic",
            "_get",
            "_set",
            "_isset",
            "_unset",
            "_sleep",
            "_wakeup",
            "_toString",
            "_invoke",
            "_set_state",
            "_clone",
            "_debugInfo"
    );

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (invalidNames.contains(method.getName())) {
            final String message = problemDescription.replace("%m%", method.getName());
            holder.registerProblem(method.getNameIdentifier(), message, ProblemHighlightType.WEAK_WARNING);
        }
    }
}
