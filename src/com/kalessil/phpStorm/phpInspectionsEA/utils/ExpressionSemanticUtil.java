package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

public class ExpressionSemanticUtil {
    /**
     * @param ifStatement if expression to check
     * @return boolean
     */
    public static boolean hasAlternativeBranches (If ifStatement){
        return (
            null != ifStatement.getElseBranch() ||
            ifStatement.getElseIfBranches().length > 0
        );
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
}
