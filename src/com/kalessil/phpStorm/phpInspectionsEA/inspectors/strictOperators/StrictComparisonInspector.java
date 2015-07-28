package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictComparisonInspector extends BasePhpInspection {
    private static final String strProblemDescriptionComparisonString = "Use binary-safe strcmp function instead of direct strings comparison.";
    private static final String strProblemDescriptionComparison = "Not numeric types in comparison (%t1% and %t2%).";

    @NotNull
    public String getShortName() {
        return "StrictComparisonInspection";
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
                if (operation.equals("<") || operation.equals("<=") || operation.equals(">") || operation.equals(">=") || operation.equals("<=>")) {
                    final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                    final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);
                    inspectBinaryComparison(expr, leftT, rightT);
                }
            }

            private void inspectBinaryComparison(final BinaryExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isNumeric() && rightT.isNumeric()) {
                    return;
                }

                final String strWarning;
                if (leftT.isString() && rightT.isString()) {
                    strWarning = strProblemDescriptionComparisonString;
                } else {
                    strWarning = strProblemDescriptionComparison
                            .replace("%t1%", leftT.toString())
                            .replace("%t2%", rightT.toString());
                }
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
