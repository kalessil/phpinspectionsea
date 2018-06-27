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

import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NestedPositiveIfStatementsInspector extends BasePhpInspection {
    private static final String message = "If construct can be merged with parent one.";

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
                final PsiElement parent = expression.getParent();
                if (parent instanceof GroupStatement) {
                    final PsiElement parentConstruct = parent.getParent();
                    if (parentConstruct instanceof If) {
                        final If parentIf = (If) parentConstruct;
                        if (this.worthMerging(expression.getCondition(), parentIf.getCondition())) {
                            final boolean isTarget =
                                !ExpressionSemanticUtil.hasAlternativeBranches(expression) &&
                                !ExpressionSemanticUtil.hasAlternativeBranches(parentIf) &&
                                ExpressionSemanticUtil.countExpressionsInGroup((GroupStatement) parent) == 1;
                            if (isTarget) {
                                holder.registerProblem(
                                        expression.getFirstChild(),
                                        message,
                                        new MergeIntoParentIfFix(expression, parentIf)
                                );
                            }
                        }
                    } else if (parentConstruct instanceof Else) {
                        final boolean isTarget = ExpressionSemanticUtil.countExpressionsInGroup((GroupStatement) parent) == 1;
                        if (isTarget) {
                            holder.registerProblem(
                                    expression.getFirstChild(),
                                    message,
                                    new MergeIntoParentElseFix(expression, (Else) parentConstruct)
                            );
                        }
                    }
                }
            }

            private boolean worthMerging(@Nullable PsiElement condition, @Nullable PsiElement parentCondition) {
                return Stream.of(condition, parentCondition)
                        .filter(expression    -> expression instanceof BinaryExpression)
                        .noneMatch(expression -> {
                            final IElementType operator = ((BinaryExpression) expression).getOperationType();
                            return operator == PhpTokenTypes.opOR || operator == PhpTokenTypes.opLIT_OR;
                        });
            }
        };
    }

    private static final class MergeIntoParentElseFix implements LocalQuickFix {
        private static final String title = "Merge into parent construct";

        final private SmartPsiElementPointer<If> target;
        final private SmartPsiElementPointer<Else> parent;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title + " (else)";
        }

        MergeIntoParentElseFix(@NotNull If target, @NotNull Else parent) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(target.getProject());

            this.target = factory.createSmartPsiElementPointer(target);
            this.parent = factory.createSmartPsiElementPointer(parent);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final If target   = this.target.getElement();
            final Else parent = this.parent.getElement();
            if (target != null && parent != null && !project.isDisposed()) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(parent);
                if (body != null) {
                    body.replace(target);
                }
            }
        }
    }

    private static final class MergeIntoParentIfFix implements LocalQuickFix {
        private static final String title = "Merge into parent construct";

        final private SmartPsiElementPointer<If> target;
        final private SmartPsiElementPointer<If> parent;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title + " (if)";
        }

        MergeIntoParentIfFix(@NotNull If target, @NotNull If parent) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(target.getProject());

            this.target = factory.createSmartPsiElementPointer(target);
            this.parent = factory.createSmartPsiElementPointer(parent);
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
                        final String code        = String.format("(%s && %s)", parentCondition.getText(), condition.getText());
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
