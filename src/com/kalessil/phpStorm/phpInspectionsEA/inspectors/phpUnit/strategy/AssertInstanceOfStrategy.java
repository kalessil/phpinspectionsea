package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.BinaryExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertInstanceOfStrategy {
    final static String message = "assertInstanceOf should be used instead";

    static public void apply(@NotNull String function, MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (1 == params.length && function.equals("assertTrue")) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof BinaryExpressionImpl) {
                BinaryExpressionImpl instance = (BinaryExpressionImpl) param;
                if (
                    null == instance.getOperation() || null == instance.getRightOperand() || null == instance.getLeftOperand() ||
                    PhpTokenTypes.kwINSTANCEOF != instance.getOperation().getNode().getElementType()
                ) {
                    return;
                }

                final TheLocalFix fixer = new TheLocalFix(instance.getRightOperand(), instance.getLeftOperand());
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);
            }
        }
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement classIdentity;
        private PsiElement subject;

        TheLocalFix(@NotNull PsiElement classIdentity, @NotNull PsiElement subject) {
            super();
            this.classIdentity = classIdentity;
            this.subject       = subject;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertInstanceOf";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                if (this.classIdentity instanceof ClassReferenceImpl) {
                    final String fqn = ((ClassReferenceImpl) this.classIdentity).getFQN();
                    if (!StringUtil.isEmpty(fqn)) {
                        final String pattern = "'" + fqn.replaceAll("\\\\", "\\\\\\\\") + "'"; // <- I hate Java escaping
                        this.classIdentity = PhpPsiElementFactory.createFromText(project, StringLiteralExpressionImpl.class, pattern);
                    }
                }

                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "pattern(null, null)");
                replacement.getParameters()[0].replace(this.classIdentity);
                replacement.getParameters()[1].replace(this.subject);

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertInstanceOf");
            }
        }
    }

}
