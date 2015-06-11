package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;

import java.util.HashMap;
import java.util.HashSet;

public class MustNotThrowExceptionsStrategy {
    private static final String strProblemDescription   = "%m%: exceptions must not be raised (%c% thrown)";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (null == method.getNameIdentifier()) {
            return;
        }

        HashSet<PsiElement> processedRegistry = new HashSet<PsiElement>();
        HashMap<PhpClass, HashSet<PsiElement>> throwsExceptions = CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
        processedRegistry.clear();

        if (throwsExceptions.size() > 0) {
            for (PhpClass thrown : throwsExceptions.keySet()) {
                String strMessage = strProblemDescription
                        .replace("%c%", thrown.getFQN())
                        .replace("%m%", method.getName());

                for (PsiElement blame : throwsExceptions.get(thrown)) {
                    holder.registerProblem(blame, strMessage, ProblemHighlightType.GENERIC_ERROR);
                }

                throwsExceptions.get(thrown).clear();
            }

            throwsExceptions.clear();
        }
    }
}
