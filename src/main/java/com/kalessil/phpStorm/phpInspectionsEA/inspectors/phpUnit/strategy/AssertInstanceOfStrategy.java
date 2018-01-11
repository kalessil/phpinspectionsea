package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class AssertInstanceOfStrategy {
    private final static String message = "assertInstanceOf should be used instead.";

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (params.length > 0 && (function.equals("assertTrue") || function.equals("assertNotFalse"))) {
            final PsiElement param = ExpressionSemanticUtil.getExpressionTroughParenthesis(params[0]);
            if (param instanceof BinaryExpression) {
                final BinaryExpression instance = (BinaryExpression) param;
                final PsiElement left           = instance.getLeftOperand();
                final PsiElement right          = instance.getRightOperand();
                if (right == null || left == null || instance.getOperationType() != PhpTokenTypes.kwINSTANCEOF) {
                    return false;
                }

                holder.registerProblem(reference, message, new TheLocalFix(right, left));
                return true;
            }
        }

        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> classIdentity;
        final private SmartPsiElementPointer<PsiElement> subject;

        TheLocalFix(@NotNull PsiElement classIdentity, @NotNull PsiElement subject) {
            super();
            final SmartPointerManager manager = SmartPointerManager.getInstance(classIdentity.getProject());

            this.classIdentity = manager.createSmartPsiElementPointer(classIdentity);
            this.subject       = manager.createSmartPsiElementPointer(subject);
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
            final PsiElement subject    = this.subject.getElement();
            PsiElement classIdentity    = this.classIdentity.getElement();
            if (expression instanceof FunctionReference && classIdentity != null && subject != null && !project.isDisposed()) {
                if (classIdentity instanceof ClassReference) {
                    final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                    final boolean useClassConstant    = phpVersion.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST);

                    if (useClassConstant) {
                        /* since PHP 5.5 we can use ::class constant */
                        classIdentity = PhpPsiElementFactory.createFromText(
                                project,
                                ClassConstantReference.class,
                                classIdentity.getText() + "::class"
                        );
                    } else {
                        final String fqn = ((ClassReference) classIdentity).getFQN();
                        if (fqn != null) {
                            classIdentity = PhpPsiElementFactory.createFromText(
                                    project,
                                    StringLiteralExpression.class,
                                    "'" + fqn.replaceAll("\\\\", "\\\\\\\\") + "'"
                            );
                        }
                    }
                }

                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 2 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(classIdentity);
                replaceParams[1].replace(subject);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[1]);
                }

                final FunctionReference call = (FunctionReference) expression;
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertInstanceOf");
            }
        }
    }
}
