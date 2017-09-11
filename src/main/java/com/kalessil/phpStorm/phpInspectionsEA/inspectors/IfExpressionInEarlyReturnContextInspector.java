package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfExpressionInEarlyReturnContextInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Consider refactoring the statement, " +
            "so it follows the early return approach.";

    @NotNull
    public String getShortName() {
        return "IfExpressionInEarlyReturnContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                if (!(ifStatement.getParent() instanceof GroupStatement)) {
                    return;
                }

                /* ensure if has no alternative branches as well */
                if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }

                /* ensure it's right context */
                GroupStatement objGroupExpression = (GroupStatement) ifStatement.getParent();
                final boolean isTargetContext = (
                    objGroupExpression.getParent() instanceof ForeachStatement ||
                    objGroupExpression.getParent() instanceof Method ||
                    objGroupExpression.getParent() instanceof Function ||
                    objGroupExpression.getParent() instanceof For
                );
                if (!isTargetContext) {
                    return;
                }


                /* ensure that if is single expression in group */
                int intCountStatementsInParentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupExpression);
                if (intCountStatementsInParentGroup > 1) {
                    return;
                }

                /* point the problem out */
                holder.registerProblem(ifStatement.getFirstChild(), strProblemDescription);
            }
        };
    }
}
