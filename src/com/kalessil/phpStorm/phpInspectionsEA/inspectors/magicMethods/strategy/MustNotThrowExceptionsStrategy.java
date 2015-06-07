package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;

import java.util.HashSet;

public class MustNotThrowExceptionsStrategy {
    private static final String strProblemDescription   = "%m% must not throw exceptions";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (null == method.getNameIdentifier()) {
            return;
        }

        HashSet<PsiElement> processedRegistry = new HashSet<PsiElement>();
        HashSet<PhpClass> throwsExceptions = CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
        processedRegistry.clear();

        if (throwsExceptions.size() > 0) {
            String strMessage = strProblemDescription.replace("%m%", method.getName());
            holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.GENERIC_ERROR);

            throwsExceptions.clear();
        }
    }
}
