package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.lang.psi.elements.If;

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
}
