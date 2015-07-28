package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictStringConcatInspector extends BasePhpInspection {
    private static final String strProblemDescriptionConcat = "Not string types in string concatenation operation (%t1% . %t2%). Use explicit (string) conversion to convert values to strings.";

    @NotNull
    public String getShortName() {
        return "StrictStringConcatInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(final BinaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();
                if (!operation.equals(".")) {
                    return;
                }

                final PhpExpressionTypes leftT = new PhpExpressionTypes(expr.getLeftOperand(), holder);
                final PhpExpressionTypes rightT = new PhpExpressionTypes(expr.getRightOperand(), holder);
                if (leftT.isString() && rightT.isString()) {
                    return;
                }

                final String strWarning = strProblemDescriptionConcat
                        .replace("%t1%", leftT.toString())
                        .replace("%t2%", rightT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
