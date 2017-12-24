package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
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
                        if (ifCondition != null) {
                            final IElementType operation = this.getOperator(ifStatement);
                            if (operation != null && operation == this.getOperator((If) parent)) {
                                holder.registerProblem(
                                    ifStatement.getFirstChild(),
                                    message,
                                    new MergeIfsFix(ifStatement, (If) parent, operation)
                                );
                            }
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

    private static class MergeIfsFix implements LocalQuickFix {
        final private SmartPsiElementPointer<If> target;
        final private SmartPsiElementPointer<If> parent;
        final private IElementType operation;

        @NotNull
        @Override
        public String getName() {
            return "Merge if-statements";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        MergeIfsFix(@NotNull If target, @NotNull If parent, @NotNull IElementType operation) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(target.getProject());

            this.target    = factory.createSmartPsiElementPointer(target);
            this.parent    = factory.createSmartPsiElementPointer(parent);
            this.operation = operation;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final If target = this.target.getElement();
            final If parent = this.parent.getElement();
            if (target != null && parent != null && !project.isDisposed()) {
                final PsiElement condition       = target.getCondition();
                final PsiElement body            = target.getStatement();
                final PsiElement parentCondition = parent.getCondition();
                final PsiElement parentBody      = parent.getStatement();
                if (condition != null && parentCondition != null && body != null && parentBody != null) {
                    final String code = String.format(
                        "(%s %s %s)",
                        parentCondition.getText(),
                        PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(this.operation) ? "&&" : "||",
                        condition.getText()
                    );
                    final PsiElement implant = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument();
                    if (implant != null) {
                        parentBody.replace(body);
                        parentCondition.replace(implant);
                    }
                }
            }
        }
    }
}
