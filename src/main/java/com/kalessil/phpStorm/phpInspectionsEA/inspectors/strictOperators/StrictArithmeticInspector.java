package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictArithmeticInspector extends BasePhpInspection {
    private static final String strProblemDescriptionBinaryPlus = "Wrong types in '+' operation (%t1% + %t2%).";
    private static final String strProblemDescriptionBinaryArithmetic = "Non numeric types in arithmetic operation (%t1% and %t2%).";
    private static final String strProblemDescriptionBinaryMod = "Non integer types in '%' operation (%t1% % %t2%).";

    @NotNull
    public String getShortName() {
        return "StrictArithmeticInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(final BinaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("+")) {
                    inspectBinaryPlus(expr, leftT, rightT);
                } else if (operation.equals("-") || operation.equals("*") || operation.equals("/") || operation.equals("**")) {
                    inspectBinaryArithmetic(expr, leftT, rightT);
                } else if(operation.equals("%")) {
                    inspectBinaryMod(expr, leftT, rightT);
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
                if (operation.equals("+=")) {
                    inspectBinaryPlus(expr, leftT, rightT);
                } else if (operation.equals("-=") || operation.equals("*=") || operation.equals("/=") || operation.equals("**=")) {
                    inspectBinaryArithmetic(expr, leftT, rightT);
                } else if (operation.equals("%=")) {
                    inspectBinaryMod(expr, leftT, rightT);
                }
            }

            private void inspectBinaryPlus(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isNumeric() && rightT.isNumeric()) {
                    // numeric addition
                    return;
                }
                if (leftT.isArray() && rightT.isArray()) {
                    // array merging
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryPlus
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryArithmetic(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isNumeric() && rightT.isNumeric()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryArithmetic
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryMod(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isInt() && rightT.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryMod
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
