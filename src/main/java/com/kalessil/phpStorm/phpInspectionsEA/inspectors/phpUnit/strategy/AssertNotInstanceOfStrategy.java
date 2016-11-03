package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.BinaryExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertNotInstanceOfStrategy {
    private final static String message = "assertNotInstanceOf should be used instead";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 0 && (function.equals("assertFalse") || function.equals("assertNotTrue"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof BinaryExpressionImpl) {
                final BinaryExpressionImpl instance = (BinaryExpressionImpl) param;
                if (
                    null == instance.getOperation() || null == instance.getRightOperand() || null == instance.getLeftOperand() ||
                    PhpTokenTypes.kwINSTANCEOF != instance.getOperation().getNode().getElementType()
                ) {
                    return false;
                }

                final TheLocalFix fixer = new TheLocalFix(instance.getRightOperand(), instance.getLeftOperand());
                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);

                return true;
            }
        }

        return false;
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
            return "Use ::assertNotInstanceOf";
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
                    final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                    final boolean useClassConstant    = phpVersion.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST);

                    if (useClassConstant) {
                        /* since PHP 5.5 we can use ::class constant */
                        final String pattern = this.classIdentity.getText() + "::class";
                        this.classIdentity = PhpPsiElementFactory.createFromText(project, ClassConstantReferenceImpl.class, pattern);
                    } else {
                        final String fqn = ((ClassReferenceImpl) this.classIdentity).getFQN();
                        if (!StringUtil.isEmpty(fqn)) {
                            final String pattern = "'" + fqn.replaceAll("\\\\", "\\\\\\\\") + "'"; // <- I hate Java escaping
                            this.classIdentity = PhpPsiElementFactory.createFromText(project, StringLiteralExpressionImpl.class, pattern);
                        }
                    }
                }

                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 2 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(this.classIdentity);
                replaceParams[1].replace(this.subject);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotInstanceOf");
            }

            /* release a tree node reference */
            this.classIdentity = null;
            this.subject       = null;
        }
    }
}
