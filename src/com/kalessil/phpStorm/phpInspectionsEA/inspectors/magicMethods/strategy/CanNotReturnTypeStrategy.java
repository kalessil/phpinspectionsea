package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;

import java.util.Collection;

public class CanNotReturnTypeStrategy {
    private static final String strProblemDescription = "%m% cannot return any value";

    static public void apply(final Method method, final ProblemsHolder holder) {
        Collection<PhpReturn> returnStatements = PsiTreeUtil.findChildrenOfType(method, PhpReturn.class);

        if (returnStatements.size() > 0 && null != method.getNameIdentifier()) {
            String strMessage = strProblemDescription.replace("%m%", method.getName());
            for (PhpReturn returnExpression : returnStatements) {
                PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue(returnExpression);
                if (null != returnValue && method == ExpressionSemanticUtil.getScope(returnExpression)) {
                    holder.registerProblem(returnExpression, strMessage, ProblemHighlightType.ERROR);
                }
            }
        }

        returnStatements.clear();
    }
}
