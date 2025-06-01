package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.elements.PhpThrowExpression;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

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
    @Override
    public String getShortName() {
        return "ThrowRawExceptionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "General exception is thrown";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpThrowExpression(@NotNull PhpThrowExpression expression) {
                final PsiElement argument = expression.getArgument();
                if (argument instanceof NewExpression newExpression) {
                    final ClassReference classReference = newExpression.getClassReference();
                    final String classFqn               = classReference == null ? null : classReference.getFQN();
                    if (classFqn != null) {
                        if (classFqn.equals("\\Exception")) {
                            holder.registerProblem(
                                    classReference,
                                    MessagesPresentationUtil.prefixWithEa(messageRawException),
                                    new TheLocalFix()
                            );
                        } else if (REPORT_MISSING_ARGUMENTS && newExpression.getParameters().length == 0) {
                            final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(classFqn, PhpIndex.getInstance(holder.getProject()));
                            if (classes.size() == 1 && this.isTarget(classes.iterator().next())) {
                                holder.registerProblem(
                                        newExpression,
                                        MessagesPresentationUtil.prefixWithEa(messageNoArguments)
                                );
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
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
