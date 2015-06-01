package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictTernaryInspector extends BasePhpInspection {
    private static final String strProblemDescriptionTernary = "Different types of true (%t1%) and false (%t2%) branches of ternary expression.";

    @NotNull
    public String getShortName() {
        return "StrictTernaryInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(final TernaryExpression expr) {
                final PhpExpressionTypes firstT = new PhpExpressionTypes(expr.getTrueVariant(), holder);
                final PhpExpressionTypes secondT = new PhpExpressionTypes(expr.getFalseVariant(), holder);
                if (firstT.equals(secondT)) {
                    return;
                }

                final String strWarning = strProblemDescriptionTernary
                        .replace("%t1%", firstT.toString())
                        .replace("%t2%", secondT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
