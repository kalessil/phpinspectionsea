package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use square brackets instead of curvy for deep analysis.";


    @NotNull
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            public void visitPhpArrayAccessExpression(ArrayAccessExpression expression) {
                PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode) {
                    return;
                }

                // recommend to use [] instead of {}
                if (bracketNode.getText().equals("}")) {
                    holder.registerProblem(expression, strProblemUseSquareBrackets, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                // ensure value is one of (array, string, \ArrayAccess, \SimpleXMLElement)
                // => general error otherwise

                // ensure index is one of (string, float, bool, null)
                // => general error otherwise
            }
        };
    }
}
