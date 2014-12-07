package com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfExpressionInEarlyReturnContextInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Consider refactoring the statement, " +
            "so it follows early return approach.";

    @NotNull
    public String getDisplayName() {
        return "Code smell: early returns can be used";
    }

    @NotNull
    public String getShortName() {
        return "IfExpressionInEarlyReturnContextInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                if (!(ifStatement.getParent() instanceof GroupStatement)) {
                    return;
                }

                /** ensure if has no alternative branches as well */
                if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }

                /** ensure it's right context */
                GroupStatement objGroupExpression = (GroupStatement) ifStatement.getParent();
                final boolean isTargetContext = (
                    objGroupExpression.getParent() instanceof ForeachStatement ||
                    objGroupExpression.getParent() instanceof Function ||
                    objGroupExpression.getParent() instanceof For
                );
                if (!isTargetContext) {
                    return;
                }


                /** ensure that if is single expression in group */
                int intCountStatementsInParentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupExpression);
                if (intCountStatementsInParentGroup > 1) {
                    return;
                }

                /** point the problem out */
                holder.registerProblem(ifStatement.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}