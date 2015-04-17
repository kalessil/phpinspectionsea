package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpThrow;

import java.util.Collection;

public class MustNotThrowExceptionsStrategy {
    private static final String strProblemDescription = "%m% must not throw exceptions";

    static public void apply(final Method method, final ProblemsHolder holder) {
        Collection<PhpThrow> throwStatements = PsiTreeUtil.findChildrenOfType(method, PhpThrow.class);

        if (throwStatements.size() > 0 && null != method.getNameIdentifier()) {
            String strMessage = strProblemDescription.replace("%m%", method.getName());
            for (PhpThrow throwExpression : throwStatements) {
                holder.registerProblem(throwExpression, strMessage, ProblemHighlightType.ERROR);
            }
        }

        throwStatements.clear();
    }
}
