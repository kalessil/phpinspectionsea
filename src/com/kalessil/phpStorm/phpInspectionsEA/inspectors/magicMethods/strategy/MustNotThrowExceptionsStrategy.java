package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpThrow;

import java.util.Collection;

public class MustNotThrowExceptionsStrategy {
    private static final String strProblemDescription   = "%m% must not throw exceptions";
    private static final String strProblemInternalCalls = "%m%'s internal calls throws exceptions";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (null != method.getNameIdentifier()) {
            return;
        }

        /* explicit throw statements */
        Collection<PhpThrow> throwStatements = PsiTreeUtil.findChildrenOfType(method, PhpThrow.class);
        if (throwStatements.size() > 0) {
            String strMessage = strProblemDescription.replace("%m%", method.getName());
            for (PhpThrow throwExpression : throwStatements) {
                holder.registerProblem(throwExpression, strMessage, ProblemHighlightType.GENERIC_ERROR);
            }
        }
        throwStatements.clear();

        /* check what internal calls can bring runtime */
//        HashSet<String> possible = new HashSet<String>();
//        CollectPossibleThrowsUtil.resolveThrownExceptions(method, possible);
//        if (possible.size() > 0) {
//            String strError = strProblemInternalCalls.replace("%m%", method.getName());
//            holder.registerProblem(method.getNameIdentifier(), strError, ProblemHighlightType.GENERIC_ERROR);
//        }
//        possible.clear();
    }
}
