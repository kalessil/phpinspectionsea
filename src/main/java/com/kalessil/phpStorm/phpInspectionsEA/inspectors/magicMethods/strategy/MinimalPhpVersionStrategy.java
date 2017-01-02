package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.Method;

public class MinimalPhpVersionStrategy {
    private static final String strProblemDescription = "%m% is introduced only in version %v%, hence it's unused.";

    static public void apply(final Method method, final ProblemsHolder holder, final PhpLanguageLevel neededVersion) {
        if (null != method.getNameIdentifier()) {
            final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
            if (phpVersion.compareTo(neededVersion) < 0) { // at least required version
                final String message = strProblemDescription
                        .replace("%m%", method.getName())
                        .replace("%v%", neededVersion.getVersionString());
                holder.registerProblem(method.getNameIdentifier(), message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
            }
        }
    }
}
