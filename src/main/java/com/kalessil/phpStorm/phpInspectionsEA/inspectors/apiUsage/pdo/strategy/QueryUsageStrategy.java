package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.utils.MethodIdentityUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class QueryUsageStrategy {
    private static final String message = "'PDO::query(...)' should be used instead of 'prepare-execute' calls chain.";

    public static void apply(@NotNull MethodReference reference, @NotNull final ProblemsHolder holder) {
        /* check requirements */
        final PsiElement[] params = reference.getParameters();
        final String methodName   = reference.getName();
        if (params.length > 0 || methodName == null || !methodName.equals("execute")) {
            return;
        }

        /* inspect preceding and succeeding statement */
        final PsiElement parent = reference.getParent();
        PsiElement predecessor  = null;
        if (OpenapiTypesUtil.isStatementImpl(parent)) {
            predecessor = ((Statement) parent).getPrevPsiSibling();
            while (predecessor instanceof PhpDocComment) {
                predecessor = ((PhpDocComment) predecessor).getPrevPsiSibling();
            }
        }
        if (null != predecessor && predecessor.getFirstChild() instanceof AssignmentExpression) {
            /* predecessor needs to be an assignment */
            final AssignmentExpression assignment = (AssignmentExpression) predecessor.getFirstChild();
            if (!(assignment.getValue() instanceof MethodReference)) {
                return;
            }
            final MethodReference precedingReference = (MethodReference) assignment.getValue();
            if (MethodIdentityUtil.isReferencingMethod(precedingReference, "\\PDO", "prepare")) {
                final PsiElement variableAssigned = assignment.getVariable();
                final PsiElement variableUsed     = reference.getClassReference();
                if (
                    variableAssigned != null && variableUsed != null &&
                    PsiEquivalenceUtil.areElementsEquivalent(variableAssigned, variableUsed)
                ) {
                    holder.registerProblem(reference, message, new UseQueryFix(precedingReference));
                }

            }
        }
    }

    private static class UseQueryFix implements LocalQuickFix {
        @NotNull
        private final SmartPsiElementPointer<MethodReference> prepare;

        UseQueryFix(@NotNull MethodReference prepare) {
            super();
            SmartPointerManager manager =  SmartPointerManager.getInstance(prepare.getProject());

            this.prepare = manager.createSmartPsiElementPointer(prepare);
        }

        @NotNull
        @Override
        public String getName() {
            return "Use '->query(...)' instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression   = descriptor.getPsiElement();
            final MethodReference prepare = this.prepare.getElement();
            if (null != prepare && expression instanceof MethodReference) {
                final PsiElement execute = expression.getParent();
                if (execute.getPrevSibling() instanceof PsiWhiteSpace) {
                    execute.getPrevSibling().delete();
                }
                execute.delete();

                prepare.handleElementRename("query");
            }
        }
    }
}
