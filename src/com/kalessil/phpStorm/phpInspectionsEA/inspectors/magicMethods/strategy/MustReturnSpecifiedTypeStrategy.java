package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;

import java.util.Collection;

public class MustReturnSpecifiedTypeStrategy {
    private static final String strProblemDescription = "%m% must return %t%";

    static public void apply(final PhpType allowedTypes, final Method method, final ProblemsHolder holder) {
        Collection<PhpReturn> returnStatements = PsiTreeUtil.findChildrenOfType(method, PhpReturn.class);

        if (returnStatements.size() > 0 && null != method.getNameIdentifier()) {
            String strMessage = strProblemDescription
                    .replace("%m%", method.getName())
                    .replace("%t%", allowedTypes.toString());

            for (PhpReturn returnExpression : returnStatements) {
                PhpExpression returnValue        = ExpressionSemanticUtil.getReturnValue(returnExpression);
                PsiElement returnValueExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(returnValue);
                if (returnValueExpression instanceof PhpTypedElement) {
                    PhpType argumentType = PhpRefactoringUtil.getCompletedType((PhpTypedElement) returnValueExpression, holder.getProject());
                    if (PhpType.isSubType(argumentType, allowedTypes) || method != ExpressionSemanticUtil.getScope(returnExpression)) {
                        /* safe escape path for legal cases */
                        continue;
                    }
                }

                holder.registerProblem(returnExpression, strMessage, ProblemHighlightType.ERROR);
            }
        }

        returnStatements.clear();
    }
}
