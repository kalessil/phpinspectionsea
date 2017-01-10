package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

public class TakesExactAmountOfArgumentsStrategy {
    private static final String messagePattern = "%m% accepts exactly %n% arguments.";

    static public void apply(int argumentsCount, @NotNull Method method, @NotNull ProblemsHolder holder) {
        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
        if (null != nameNode && argumentsCount != method.getParameters().length) {
            final String message = messagePattern
                    .replace("%m%", method.getName())
                    .replace("%n%", String.valueOf(argumentsCount));
            holder.registerProblem(nameNode, message, ProblemHighlightType.ERROR);
        }
    }
}
