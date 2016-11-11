package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NestedPositiveIfStatementsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "If statement can be merged into parent.";

    @NotNull
    public String getShortName() {
        return "NestedPositiveIfStatementsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /* meet pre-conditions */
                PsiElement objParent = ifStatement.getParent();
                if (!(objParent instanceof GroupStatement)) {
                    return;
                }
                objParent = objParent.getParent();
                if (!(objParent instanceof If)) {
                    return;
                }

                /* ensure parent if and the expression has no alternative branches */
                if (
                    ExpressionSemanticUtil.hasAlternativeBranches(ifStatement) ||
                    ExpressionSemanticUtil.hasAlternativeBranches((If) objParent)
                ) {
                    return;
                }

                /* ensure that if is single expression in group */
                GroupStatement objGroupStatement = (GroupStatement) ifStatement.getParent();
                int intCountStatementsInParentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement);
                if (intCountStatementsInParentGroup > 1) {
                    return;
                }

                /* ensure that the same logical operator being used (to not increase the visual complexity) */
                IElementType operator       = getOperator(ifStatement);
                IElementType parentOperator = getOperator((If) objParent);
                if (operator != parentOperator) {
                    return;
                }

                /* point on the issues */
                PhpPsiElement objIfCondition = ifStatement.getCondition();
                if (objIfCondition == null) {
                    return;
                }

                holder.registerProblem(objIfCondition, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }

            private @Nullable IElementType getOperator(final @NotNull If statement) {
                /* no condition or single condition*/
                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                if (null == condition) {
                    return null;
                }
                if (!(condition instanceof BinaryExpression)) {
                    return PhpTokenTypes.opAND;
                }

                /* we need only or/and operators to return */
                final PsiElement operation = ((BinaryExpression) condition).getOperation();
                if (null == operation) {
                    return null;
                }
                final IElementType operationType = operation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opOR && operationType != PhpTokenTypes.opAND) {
                    return null;
                }
                return operationType;
            }
        };
    }
}
