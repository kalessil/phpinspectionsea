package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictBooleanNotInspector extends BasePhpInspection {
    private static final String strProblemDescriptionBooleanNot = "Logical-not detected before %t% type operand.";

    @NotNull
    public String getShortName() {
        return "StrictBooleanNotInspection";
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
                if (!operation.equals("!") && !operation.equals("not")) {
                    return;
                }

                final PhpExpressionTypes type = new PhpExpressionTypes(expr, holder);
                if (type.isBoolean()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBooleanNot
                        .replace("%t%", type.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
