package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictIncrementInspector extends BasePhpInspection {
    private static final String strProblemDescriptionIncrement = "Increment/decrement of %t% type operand.";

    @NotNull
    public String getShortName() {
        return "StrictIncrementInspection";
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

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("++") || operation.equals("--")) {
                    final PhpExpressionTypes type = new PhpExpressionTypes(expr, holder);
                    inspectUnaryIncrement(expr, type);
                }
            }

            private void inspectUnaryIncrement(final UnaryExpression expr, final PhpExpressionTypes type) {
                if (type.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionIncrement
                        .replace("%t%", type.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
