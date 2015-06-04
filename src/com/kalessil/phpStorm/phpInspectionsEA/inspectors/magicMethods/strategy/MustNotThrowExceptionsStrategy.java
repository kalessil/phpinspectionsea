package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpThrow;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;

import java.util.Collection;
import java.util.HashSet;

public class MustNotThrowExceptionsStrategy {
    private static final String strProblemDescription   = "%m% must not throw exceptions";
    private static final String strProblemInternalCalls = "%m%'s internal calls can throw exceptions";

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
        HashSet<String> possible = new HashSet<String>();
        CollectPossibleThrowsUtil.collectAnnotatedExceptions(method, possible);

        /* obtain declared, overhead but fine for background analysis */
        HashSet<String> declared = new HashSet<String>();
        ThrowsResolveUtil.ResolveType resolvingStatus = ThrowsResolveUtil.resolveThrownExceptions(method, declared);
        if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == resolvingStatus) {
            declared.clear();
            possible.clear();
            return;
        }

        /* check possible - declared are covered properly */
        for (String onePossible : possible) {
            if (onePossible.indexOf('\\') == -1 && !declared.contains(onePossible)) {
                String strError = strProblemInternalCalls.replace("%m%", onePossible);
                holder.registerProblem(method.getNameIdentifier(), strError, ProblemHighlightType.GENERIC_ERROR);
            }
        }

        declared.clear();
        possible.clear();
    }
}
