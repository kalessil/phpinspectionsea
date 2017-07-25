package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SelfClassReferencingInspector extends BasePhpInspection {
    private static final String messageReplacement = "Class reference \"%s\" could be replaced by \"%s\"";

    @SuppressWarnings ("WeakerAccess") public boolean optionPreferClass;

    @NotNull
    public String getShortName() {
        return "SelfClassReferencingInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean b) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpNewExpression(final NewExpression expression) {
                validateCommonComponent(expression, expression.getClassReference());
            }

            @Override
            public void visitPhpMethodReference(final MethodReference reference) {
                if (reference.isStatic()) {
                    validateCommonComponent(reference, (PhpReference) reference.getClassReference());
                }
            }

            @Override
            public void visitPhpClassConstantReference(final ClassConstantReference constantReference) {
                if ("class".equals(constantReference.getName())) {
                    validateClassConstantComponent(constantReference, (PhpReference) constantReference.getClassReference());
                    return;
                }

                validateCommonComponent(constantReference, (PhpReference) constantReference.getClassReference());
            }

            @Override
            public void visitPhpFieldReference(final FieldReference fieldReference) {
                if (fieldReference.isStatic()) {
                    validateCommonComponent(fieldReference, (PhpReference) fieldReference.getClassReference());
                }
            }

            @Override
            public void visitPhpConstantReference(final ConstantReference reference) {
                if (optionPreferClass && "__CLASS__".equals(reference.getName())) {
                    final PhpClass expressionParentClass = PsiTreeUtil.getParentOfType(reference, PhpClass.class);

                    if ((expressionParentClass == null) ||
                        (expressionParentClass.isAnonymous())) {
                        return;
                    }

                    registerProblem(reference, expressionParentClass.getName() + "::class", "__CLASS__");
                }
            }

            private boolean isSameFQN(@NotNull final PsiElement psiElement, @Nullable final PhpReference classReference) {
                if (classReference == null) {
                    return false;
                }

                if (optionPreferClass) {
                    return "self".equals(classReference.getName());
                }

                final String classReferenceName = classReference.getName();

                if ("self".equals(classReferenceName) ||
                    "static".equals(classReferenceName)) {
                    return false;
                }

                final PhpClass expressionParentClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);

                return (expressionParentClass != null) &&
                       (expressionParentClass.getFQN().equals(classReference.getFQN()));
            }

            @Nullable
            private String getClassName(@NotNull final PhpReference psiElement) {
                if (!optionPreferClass) {
                    return psiElement.getName();
                }

                final PhpClass expressionParentClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);

                if ((expressionParentClass == null) ||
                    (expressionParentClass.isAnonymous())) {
                    return null;
                }

                return expressionParentClass.getName();
            }

            private void validateClassConstantComponent(@NotNull final PsiElement constantReference, @Nullable final PhpReference classReference) {
                if (isSameFQN(constantReference, classReference)) {
                    if (!optionPreferClass) {
                        registerProblem(classReference.getParent(), getClassName(classReference) + "::class", "__CLASS__");
                        return;
                    }

                    final String className = getClassName(classReference);

                    if ((className == null) || (className.isEmpty())) {
                        return;
                    }

                    final String classReferenceName = classReference.getName();

                    if (classReferenceName == null) {
                        return;
                    }

                    registerProblem(classReference, className, classReferenceName);
                }
            }

            private void validateCommonComponent(final PsiElement expression, @Nullable final PhpReference classReference) {
                if (isSameFQN(expression, classReference)) {
                    final String className = getClassName(classReference);

                    if ((className == null) || (className.isEmpty())) {
                        return;
                    }

                    registerProblem(classReference, className, "self");
                }
            }

            private void registerProblem(@NotNull final PsiElement psiElement, @NotNull final String originalName, @NotNull final String replacementName) {
                if (optionPreferClass) {
                    problemsHolder.registerProblem(psiElement, String.format(messageReplacement, replacementName, originalName),
                                                   new TheLocalFix(originalName));
                    return;
                }

                problemsHolder.registerProblem(psiElement, String.format(messageReplacement, originalName, replacementName),
                                               new TheLocalFix(replacementName));
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.addCheckbox("Prefer class name referencing", optionPreferClass, (isSelected) -> optionPreferClass = isSelected));
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private final String replacementName;

        private TheLocalFix(@NotNull final String replacementName) {
            this.replacementName = replacementName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Replace to \"" + replacementName + '"';
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement operator = descriptor.getPsiElement();

            if (operator == null) {
                return;
            }

            // Applicable from __CLASS__ to MyClass::class.
            if (replacementName.endsWith("::class")) {
                final ClassConstantReference createdElement = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, replacementName + ';');

                if (createdElement == null) {
                    return;
                }

                operator.replace(createdElement);
                return;
            }

            operator.replace(PhpPsiElementFactory.createClassReference(project, replacementName));
        }
    }
}
