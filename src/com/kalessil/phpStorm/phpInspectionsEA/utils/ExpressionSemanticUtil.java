package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

public class ExpressionSemanticUtil {
    /**
     * @param ifStatement if expression to check
     * @return boolean
     */
    public static boolean hasAlternativeBranches(If ifStatement) {
        return (null != ifStatement.getElseBranch() || ifStatement.getElseIfBranches().length > 0);
    }

    /**
     * @param objGroupStatement group expression to check
     * @return integer
     */
    public static int countExpressionsInGroup(GroupStatement objGroupStatement) {
        int intCountStatements = 0;
        for (PsiElement objStatement : objGroupStatement.getChildren()) {
            if (!(objStatement instanceof PhpPsiElement)) {
                continue;
            }

            ++intCountStatements;
        }

        return intCountStatements;
    }

    public static PsiElement getLastStatement(GroupStatement objGroupStatement) {
        PsiElement objLastChild = objGroupStatement.getLastChild();
        while (null != objLastChild) {
            if (objLastChild instanceof PhpPsiElement) {
                return objLastChild;
            }

            objLastChild = objLastChild.getPrevSibling();
        }

        return null;
    }

    /**
     * @param objControlExpression expression to scan for group definition
     * @return null|GroupStatement
     */
    @Nullable
    public static GroupStatement getGroupStatement(PsiElement objControlExpression) {
        for (PsiElement objChild : objControlExpression.getChildren()) {
            if (!(objChild instanceof GroupStatement)) {
                continue;
            }

            return (GroupStatement) objChild;
        }

        return null;
    }

    /**
     * @param objConstant expression to check
     * @return boolean
     */
    public static boolean isBoolean(ConstantReference objConstant){
        return (PhpLangUtil.isTrue(objConstant) || PhpLangUtil.isFalse(objConstant));
    }

    /**
     * @param objExpression to process
     * @return inner expression
     */
    @Nullable
    public static PsiElement getExpressionTroughParenthesis(PsiElement objExpression) {
        if (!(objExpression instanceof ParenthesizedExpression)) {
            return objExpression;
        }

        PsiElement objInnerExpression = ((ParenthesizedExpression) objExpression).getArgument();
        while (objInnerExpression instanceof ParenthesizedExpression) {
            objInnerExpression = ((ParenthesizedExpression) objExpression).getArgument();
        }

        return objInnerExpression;
    }

    /**
     * get first statement: for all inspectors
     */
}
