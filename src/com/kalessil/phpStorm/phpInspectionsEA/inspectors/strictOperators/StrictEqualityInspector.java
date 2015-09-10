package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictEqualityInspector extends BasePhpInspection {
    private static final String strProblemDescriptionEqualityFloat = "Possible machine precision rounding in float comparison. It's better to compare absolute difference with a small number.";
    private static final String strProblemDescriptionEquality = "Different types in comparison (%t1% and %t2%).";

    @NotNull
    public String getShortName() {
        return "StrictEqualityInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(final BinaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("==") || operation.equals("===") || operation.equals("!=") || operation.equals("!==") || operation.equals("<>")) {
                    final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                    final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);
                    inspectBinaryEquality(expr, leftT, rightT);
                }
            }

            private void inspectBinaryEquality(final BinaryExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isInt() && rightT.isInt()) {
                    return;
                }
                if (leftT.isNumeric() && rightT.isNumeric()) {
                    holder.registerProblem(expr, strProblemDescriptionEqualityFloat, ProblemHighlightType.WEAK_WARNING);
                    return;
                }
                if (leftT.equals(rightT)) {
                    return;
                }
                if (leftT.isNull() || rightT.isNull()) {
                    // allow null values
                    return;
                }

                final String strWarning = strProblemDescriptionEquality
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
