package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
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

public class ThrowRawExceptionInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_MISSING_ARGUMENTS = true;

    private static final String messageRawException = "\\Exception is too general. Consider throwing one of SPL exceptions instead.";
    private static final String messageNoArguments  = "This exception is thrown without a message. Consider adding one to help clarify or troubleshoot the exception.";

    @NotNull
    public String getShortName() {
        return "ThrowRawExceptionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpThrow(@NotNull PhpThrow throwStatement) {
                final PsiElement argument = throwStatement.getArgument();
                if (argument instanceof NewExpression) {
                    final NewExpression newExpression   = (NewExpression) argument;
                    final ClassReference classReference = newExpression.getClassReference();
                    final String classFqn               = null == classReference ? null : classReference.getFQN();
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
                ((ClassReference) expression).handleElementRename("RuntimeException");
            }
        }
    }
}
