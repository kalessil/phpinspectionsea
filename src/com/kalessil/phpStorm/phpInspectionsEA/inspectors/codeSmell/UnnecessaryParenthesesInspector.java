package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessaryParenthesesInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "UnnecessaryParenthesesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpParenthesizedExpression(ParenthesizedExpression expression) {
                String fileName = holder.getFile().getName();
                if (fileName.endsWith(".blade.php")) {
                    /* syntax injection there is not done properly for elseif, causing false-positives */
                    return;
                }

                PhpPsiElement argument = expression.getArgument();
                PsiElement parent      = expression.getParent();
                if (null == argument || null == parent) {
                    return;
                }

                /*
                    this matrix mostly contains reasonable variants,
                    couple of them might be ambiguous, but let's keep logic simple
                */
                boolean knowsLegalCases = (
                    (
                        argument instanceof BinaryExpression   ||
                        argument instanceof TernaryExpression  ||
                        argument instanceof UnaryExpression    ||
                        argument instanceof AssignmentExpression
                    ) && (
                        parent instanceof BinaryExpression     ||
                        parent instanceof TernaryExpression    ||
                        parent instanceof UnaryExpression      ||
                        parent instanceof AssignmentExpression ||
                        parent instanceof PhpReturn
                    )
                );
                knowsLegalCases =
                    knowsLegalCases ||
                    /* some of questionable constructs, but lets start first with them */
                    parent instanceof Include ||
                    parent instanceof PhpCase ||
                    parent instanceof PhpEchoStatement ||
                    parent instanceof PhpPrintExpression ||
                    (parent instanceof ParameterList && argument instanceof TernaryExpression) ||
                    (parent instanceof MethodReference && argument instanceof NewExpression)
                ;
                if (knowsLegalCases) {
                    return;
                }

                holder.registerProblem(expression, "Unnecessary parentheses", ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove the brackets";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ParenthesizedExpression) {
                expression.replace(((ParenthesizedExpression) expression).getArgument());
            }
        }
    }
}
