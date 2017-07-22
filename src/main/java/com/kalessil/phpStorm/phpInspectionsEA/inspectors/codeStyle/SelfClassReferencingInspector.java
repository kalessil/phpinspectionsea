package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

                if (optionPreferClass &&
                    "self".equals(classReference.getName())) {
                    return true;
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

                if (expressionParentClass == null) {
                    return null;
                }

                return expressionParentClass.getName();
            }

            private void validateClassConstantComponent(@NotNull final PsiElement constantReference, @Nullable final PhpReference classReference) {
                if (isSameFQN(constantReference, classReference)) {
                    registerProblem(classReference.getParent(), getClassName(classReference) + "::class", "__CLASS__");
                }
            }

            private void validateCommonComponent(final PsiElement expression, @Nullable final PhpReference classReference) {
                if (isSameFQN(expression, classReference)) {
                    final String className = getClassName(classReference);

                    if ((className != null) && (!className.isEmpty())) {
                        registerProblem(classReference, className, "self");
                    }
                }
            }

            private void registerProblem(@NotNull final PsiElement psiElement, @NotNull final String originalName, @NotNull final String replacementName) {
                if (optionPreferClass) {
                    problemsHolder.registerProblem(psiElement, String.format(messageReplacement, replacementName, originalName));
                    return;
                }

                problemsHolder.registerProblem(psiElement, String.format(messageReplacement, originalName, replacementName));
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.addCheckbox("Prefer class name referencing", optionPreferClass, (isSelected) -> optionPreferClass = isSelected));
    }
}
