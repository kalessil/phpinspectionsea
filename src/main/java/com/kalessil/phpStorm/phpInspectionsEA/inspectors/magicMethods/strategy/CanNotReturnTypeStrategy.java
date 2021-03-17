package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

import java.util.Collection;

public class CanNotReturnTypeStrategy {
    private static final String strProblemDescription = "%m% cannot return a value.";

    static public void apply(final Method method, final ProblemsHolder holder) {
        final Collection<PhpReturn> returnStatements = PsiTreeUtil.findChildrenOfType(method, PhpReturn.class);

        if (!returnStatements.isEmpty() && NamedElementUtil.getNameIdentifier(method) != null) {
            final String message = strProblemDescription.replace("%m%", method.getName());
            for (final PhpReturn returnExpression : returnStatements) {
                final PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue(returnExpression);
                if (null != returnValue && method == ExpressionSemanticUtil.getScope(returnExpression)) {
                    holder.registerProblem(
                            returnExpression,
                            MessagesPresentationUtil.prefixWithEa(message)
                    );
                }
            }
        }

        returnStatements.clear();
    }
}
