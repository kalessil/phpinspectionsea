package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictUnaryMinusInspector extends BasePhpInspection {
    private static final String strProblemDescriptionUnaryMinus = "Unary minus detected before %t% type operand.";

    @NotNull
    public String getShortName() {
        return "StrictUnaryMinusInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnaryExpression(final UnaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();
                if (!operation.equals("-")) {
                    return;
                }

                final PhpExpressionTypes type = new PhpExpressionTypes(expr, holder);
                if (type.isNumeric()) {
                    return;
                }

                final String strWarning = strProblemDescriptionUnaryMinus
                        .replace("%t%", type.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
