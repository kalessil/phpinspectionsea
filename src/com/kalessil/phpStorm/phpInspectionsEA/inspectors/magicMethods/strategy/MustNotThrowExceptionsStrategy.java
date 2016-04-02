package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MustNotThrowExceptionsStrategy {
    private static final String messagePattern = "%m%: exceptions must not be raised (%c% thrown)";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (null == method.getNameIdentifier()) {
            return;
        }

        final HashSet<PsiElement> processedRegistry                   = new HashSet<PsiElement>();
        final HashMap<PhpClass, HashSet<PsiElement>> throwsExceptions = CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);

        processedRegistry.clear();
        if (throwsExceptions.size() > 0) {
            for (Map.Entry<PhpClass, HashSet<PsiElement>> pair : throwsExceptions.entrySet()) {
                /* extract pairs */
                final PhpClass thrown                 = pair.getKey();
                final HashSet<PsiElement> expressions = pair.getValue();

                final String message = messagePattern
                        .replace("%c%", thrown.getFQN())
                        .replace("%m%", method.getName());
                for (PsiElement blame : expressions) {
                    holder.registerProblem(blame, message, ProblemHighlightType.GENERIC_ERROR);
                }
                expressions.clear();
            }

            throwsExceptions.clear();
        }
    }
}
