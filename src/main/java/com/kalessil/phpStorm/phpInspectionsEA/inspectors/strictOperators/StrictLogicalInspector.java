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

public class StrictLogicalInspector extends BasePhpInspection {
    private static final String strProblemDescriptionBinaryLogical = "Non boolean types in logical operation (%t1%, %t2%).";

    @NotNull
    public String getShortName() {
        return "StrictLogicalInspection";
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

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("&&") || operation.equals("and") || operation.equals("||") || operation.equals("or") || operation.equals("xor")) {
                    final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                    final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);
                    inspectBinaryLogical(expr, leftT, rightT);
                }
            }

            public void visitPhpSelfAssignmentExpression(final SelfAssignmentExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                /* TODO: .getOperation().getNode().getElementType() + PhpTokenTypes.op* */
                if (operation.equals("&&=") || operation.equals("||=")) {
                    final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getFirstPsiChild(), holder);
                    final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getValue(), holder);
                    inspectBinaryLogical(expr, leftT, rightT);
                }
            }

            private void inspectBinaryLogical(final PhpExpression expr, final PhpExpressionTypes leftT, final PhpExpressionTypes rightT) {
                if (leftT.isBoolean() && rightT.isBoolean()) {
                    return;
                }

                final String strWarning = strProblemDescriptionBinaryLogical
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
