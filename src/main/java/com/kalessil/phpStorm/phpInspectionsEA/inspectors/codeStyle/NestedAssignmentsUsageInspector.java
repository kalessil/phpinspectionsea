package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NestedAssignmentsUsageInspector extends BasePhpInspection {
    private static final String message = "Using dedicated assignment would be more reliable (e.g '$... = $... + 10' can be mistyped as `$... = $... = 10`).";

    @NotNull
    @Override
    public String getShortName() {
        return "NestedAssignmentsUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression expression) {
                final PsiElement parent = expression.getParent();
                if (!(parent instanceof AssignmentExpression) && expression.getValue() instanceof AssignmentExpression) {
                    holder.registerProblem(
                            expression,
                            message,
                            OpenapiTypesUtil.isStatementImpl(parent) ? new UseDedicatedAssignmentsFix() : null
                    );
                }
            }
        };
    }

    private static final class UseDedicatedAssignmentsFix implements LocalQuickFix {
        private static final String title = "Use dedicated assignments";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof AssignmentExpression && !project.isDisposed()) {
                /* extract assignment parts */
                final List<String> parts = new ArrayList<>();
                PsiElement current       = target;
                while (current instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) current;
                    final PsiElement variable             = assignment.getVariable();
                    final PsiElement value                = assignment.getValue();
                    if (variable == null || value == null) {
                        parts.clear();
                        return;
                    }

                    parts.add(variable.getText());
                    current = value;
                }
                parts.add(current.getText());

                /* split the assignment */
                final PsiElement marker = target.getParent();
                while (parts.size() >= 2) {
                    final int last               = parts.size() - 1;
                    final PsiElement replacement = PhpPsiElementFactory.createStatement(
                            project,
                            String.format("%s = %s;", parts.get(last - 1), parts.get(last))
                    );
                    marker.getParent().addBefore(replacement, marker);
                    parts.remove(last);
                }
                marker.delete();
                parts.clear();
            }
        }
    }
}
