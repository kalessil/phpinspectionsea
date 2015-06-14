package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictBitwiseInspector extends BasePhpInspection {
    private static final String strProblemDescriptionUnaryBitwiseNot = "Unary bitwise-not detected before %t% type operand.";
    private static final String strProblemDescriptionBinaryBitwise = "Not integer types in bitwise operation (%t1% and %t2%).";
    private static final String strProblemDescriptionBinaryShift = "Not integer types in bit-shift operation (%t1% and %t2%).";

    @NotNull
    public String getShortName() {
        return "StrictBitwiseInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnaryExpression(final UnaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final PhpExpressionTypes type = new PhpExpressionTypes(expr, holder);

                final String operation = expr.getOperation().getText();
                if (operation.equals("~")) {
                    inspectUnaryBitwiseNot(expr, type);
                }
            }

            public void visitPhpBinaryExpression(final BinaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("&") || operation.equals("|") || operation.equals("^")) {
                    inspectBinaryBitwise(expr, leftT, rightT);
                } else if (operation.equals("<<") || operation.equals(">>")) {
                    inspectBinaryShift(expr, leftT, rightT);
                }
            }

            public void visitPhpSelfAssignmentExpression(final SelfAssignmentExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getFirstPsiChild(), holder);
                final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getValue(), holder);

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("&=") || operation.equals("|=") || operation.equals("^=")) {
                    inspectBinaryBitwise(expr, leftT, rightT);
                } else if (operation.equals("<<=") || operation.equals(">>=")) {
                    inspectBinaryShift(expr, leftT, rightT);
                }
            }

            private void inspectUnaryBitwiseNot(final UnaryExpression expr, final PhpExpressionTypes type) {
                if (type.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionUnaryBitwiseNot
                        .replace("%t%", type.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryBitwise(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isInt() && rightT.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryBitwise
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryShift(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isInt() && rightT.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryShift
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
