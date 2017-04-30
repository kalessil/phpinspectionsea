package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PdoApiUsageInspector extends BasePhpInspection {
    private static final String message = "'PDO::query(...)' should be used instead of 'prepare-execute' calls chain.";

    @NotNull
    public String getShortName() {
        return "PdoApiUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                /* TODO: `...->query(<single parameter>);` should be replaced by exec */

                /* check requirements */
                final PsiElement[] params = reference.getParameters();
                final String methodName   = reference.getName();
                if (params.length > 0 || methodName == null || !methodName.equals("execute")) {
                    return;
                }

                /* inspect preceding and succeeding statement */
                final PsiElement parent = reference.getParent();
                PsiElement predecessor  = null;
                PsiElement successor    = null;
                if (parent instanceof StatementImpl) {
                    predecessor = ((StatementImpl) parent).getPrevPsiSibling();
                    while (predecessor instanceof PhpDocComment) {
                        predecessor = ((PhpDocComment) predecessor).getPrevPsiSibling();
                    }

                    successor = ((StatementImpl) parent).getNextPsiSibling();
                    while (successor instanceof PhpDocComment) {
                        successor = ((PhpDocComment) successor).getNextPsiSibling();
                    }
                }
                if (null != predecessor && predecessor.getFirstChild() instanceof AssignmentExpression) {
                    /* predecessor needs to be an assignment */
                    final AssignmentExpression assignment = (AssignmentExpression) predecessor.getFirstChild();
                    if (!(assignment.getValue() instanceof MethodReference)) {
                        return;
                    }

                    /* predecessor's value is ->prepare */
                    final MethodReference precedingReference = (MethodReference) assignment.getValue();
                    final String precedingMethod             = precedingReference.getName();
                    if (null == precedingMethod || !precedingMethod.equals("prepare")) {
                        return;
                    }

                    final PsiElement variableAssigned = assignment.getVariable();
                    final PsiElement variableUsed     = reference.getClassReference();
                    if (
                        null != variableAssigned && null != variableUsed &&
                        PsiEquivalenceUtil.areElementsEquivalent(variableAssigned, variableUsed)
                    ) {
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseQueryFix(precedingReference));
                    }
                }
            }
        };
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

