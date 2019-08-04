package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ThrowRawExceptionInspector extends PhpInspection {
    // Inspection options.
    public boolean REPORT_MISSING_ARGUMENTS = true;

    private static final String messageRawException = "\\Exception is too general. Consider throwing one of SPL exceptions instead.";
    private static final String messageNoArguments  = "This exception is thrown without a message. Consider adding one to help clarify or troubleshoot the exception.";

    @NotNull
    @Override
    public String getShortName() {
        return "ThrowRawExceptionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "General '\Exception' is thrown";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpThrow(@NotNull PhpThrow throwStatement) {
                if (this.shouldSkipAnalysis(throwStatement, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement argument = throwStatement.getArgument();
                if (argument instanceof NewExpression) {
                    final NewExpression newExpression   = (NewExpression) argument;
                    final ClassReference classReference = newExpression.getClassReference();
                    final String classFqn               = classReference == null ? null : classReference.getFQN();
                    if (classFqn != null) {
                        if (classFqn.equals("\\Exception")) {
                            holder.registerProblem(classReference, messageRawException, new TheLocalFix());
                        } else if (REPORT_MISSING_ARGUMENTS && newExpression.getParameters().length == 0) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(classReference);
                            if (resolved instanceof PhpClass && this.isTarget((PhpClass) resolved)) {
                                holder.registerProblem(newExpression, messageNoArguments);
                            }
                        }
                    }
                }
            }

            private boolean isTarget(@NotNull PhpClass clazz) {
                final Method constructor = clazz.getConstructor();
                return constructor != null && constructor.getParameters().length == 3 &&
                    clazz.findOwnFieldByName("message", false) == null;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
                -> component.addCheckbox("Report omitted arguments", REPORT_MISSING_ARGUMENTS, (isSelected) -> REPORT_MISSING_ARGUMENTS = isSelected)
        );
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Throw RuntimeException instead";

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
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ClassReference && !project.isDisposed()) {
                final String namespace    = ((ClassReference) expression).getImmediateNamespaceName();
                final String newReference = (namespace.isEmpty() ? "\\" : namespace) + "RuntimeException";
                expression.replace(PhpPsiElementFactory.createClassReference(project, newReference));
            }
        }
    }
}
