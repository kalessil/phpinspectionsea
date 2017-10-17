package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NestedPositiveIfStatementsInspector extends BasePhpInspection {
    private static final String message = "If statement can be merged into parent.";

    @NotNull
    public String getShortName() {
        return "NestedPositiveIfStatementsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If ifStatement) {
                /* meet pre-conditions */
                PsiElement parent = ifStatement.getParent();
                if (parent instanceof GroupStatement) {
                    parent = parent.getParent();
                }
                /* ensure parent if and the expression has no alternative branches */
                if (
                    parent instanceof If && !ExpressionSemanticUtil.hasAlternativeBranches(ifStatement) &&
                    !ExpressionSemanticUtil.hasAlternativeBranches((If) parent)
                ) {
                    /* ensure that if is single expression in group */
                    final PsiElement directParent = ifStatement.getParent();
                    if (directParent instanceof If || (
                            directParent instanceof GroupStatement &&
                            ExpressionSemanticUtil.countExpressionsInGroup((GroupStatement) directParent) == 1
                    )) {
                        /* ensure that the same logical operator being used (to not increase the visual complexity) */
                        final PhpPsiElement ifCondition = ifStatement.getCondition();
                        if (ifCondition != null && this.getOperator(ifStatement) == this.getOperator((If) parent)) {
                            holder.registerProblem(ifStatement.getFirstChild(), message);
                        }
                    }
                }
            }

            @Nullable
            private IElementType getOperator(final @NotNull If statement) {
                /* no condition or single condition*/
                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                if (null == condition) {
                    return null;
                }
                if (!(condition instanceof BinaryExpression)) {
                    return PhpTokenTypes.opAND;
                }
                /* we need only or/and operators to return */
                final IElementType operationType = ((BinaryExpression) condition).getOperationType();
                if (
                    !PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operationType) &&
                    !PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operationType)
                ) {
                    return null;
                }
                return operationType;
            }
        };
    }
}
