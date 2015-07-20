package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictArrayAccessInspector extends BasePhpInspection {
    private static final String strProblemDescriptionArrayAccess = "This variable type does not support array access (resolved as %t%).";
    private static final String strProblemDescriptionStringAccess = "This access type shall be applied to strings only (resolved as %t%).";

    @NotNull
    public String getShortName() {
        return "StrictArrayAccessInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayAccessExpression(final ArrayAccessExpression expr) {
                final PhpExpressionTypes type = new PhpExpressionTypes(expr.getValue(), holder);
                final String strWarning;

                if (expr.getLastChild().textMatches("]")) {
                    if (type.isArray() || type.isString() || type.isArrayAccess()) {
                        return;
                    }
                    strWarning = strProblemDescriptionArrayAccess.replace("%t%", type.toString());
                } else {
                    if (type.isString()) {
                        return;
                    }
                    strWarning = strProblemDescriptionStringAccess.replace("%t%", type.toString());
                }

                holder.registerProblem(expr, strWarning, ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
