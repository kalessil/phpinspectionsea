package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

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
            public void visitPhpIf(@NotNull If expression) {
                final PsiElement parentIfBodyCandidate = expression.getParent();
                if (parentIfBodyCandidate instanceof GroupStatement) {
                    final PsiElement parentIfCandidate = parentIfBodyCandidate.getParent();
                    if (parentIfCandidate instanceof If) {
                        final If parentIf      = (If) parentIfCandidate;
                        final boolean isTarget =
                            !ExpressionSemanticUtil.hasAlternativeBranches(expression) &&
                            !ExpressionSemanticUtil.hasAlternativeBranches(parentIf) &&
                            ExpressionSemanticUtil.countExpressionsInGroup((GroupStatement) parentIfBodyCandidate) == 1;
                        if (isTarget && expression.getCondition() != null) {
                            holder.registerProblem(
                                    expression.getFirstChild(),
                                    message,
                                    new MergeIfsFix(expression, parentIf)
                            );
                        }
                    }
                }
            }
        };
    }

    private static class MergeIfsFix implements LocalQuickFix {
        final private SmartPsiElementPointer<If> target;
        final private SmartPsiElementPointer<If> parent;

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

        MergeIfsFix(@NotNull If target, @NotNull If parent) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(target.getProject());
            this.target                       = factory.createSmartPsiElementPointer(target);
            this.parent                       = factory.createSmartPsiElementPointer(parent);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final If target = this.target.getElement();
            final If parent = this.parent.getElement();
            if (target != null && parent != null && !project.isDisposed()) {
                final PsiElement condition       = target.getCondition();
                final PsiElement parentCondition = parent.getCondition();
                if (condition != null && parentCondition != null) {
                    final PsiElement body       = target.getStatement();
                    final PsiElement parentBody = parent.getStatement();
                    if (body != null && parentBody != null) {
                        final boolean escapeFirst =
                                parentCondition instanceof BinaryExpression &&
                                ((BinaryExpression) parentCondition).getOperationType() == PhpTokenTypes.opOR;
                        final boolean escapeSecond =
                                condition instanceof BinaryExpression &&
                                ((BinaryExpression) condition).getOperationType() == PhpTokenTypes.opOR;
                        final String code = String.format(
                                "(%s && %s)",
                                String.format(escapeFirst  ? "(%s)" : "%s", parentCondition.getText()),
                                String.format(escapeSecond ? "(%s)" : "%s", condition.getText())
                        );
                        final PsiElement implant = PhpPsiElementFactory
                                .createPhpPsiFromText(project, ParenthesizedExpression.class, code)
                                .getArgument();
                        if (implant != null) {
                            parentBody.replace(body);
                            parentCondition.replace(implant);
                        }
                    }
                }
            }
        }
    }
}
