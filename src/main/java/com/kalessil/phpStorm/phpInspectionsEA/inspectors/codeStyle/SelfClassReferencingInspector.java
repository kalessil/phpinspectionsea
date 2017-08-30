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
import org.apache.commons.lang3.StringUtils;
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
    private static final String messagePattern = "Class reference '%s' could be replaced by '%s'";

    // Inspection options.
    public boolean PREFER_CLASS_NAMES = false;

    @NotNull
    public String getShortName() {
        return "SelfClassReferencingInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean b) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                this.validateCommonComponent(expression, expression.getClassReference());
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (reference.isStatic()) {
                    final PhpPsiElement classReference = reference.getClassReference();
                    if (classReference instanceof PhpReference) {
                        this.validateCommonComponent(reference, (PhpReference) classReference);
                    }
                }
            }

            @Override
            public void visitPhpClassConstantReference(@NotNull ClassConstantReference constantReference) {
                if ("class".equals(constantReference.getName())) {
                    validateClassConstantComponent(constantReference, (PhpReference) constantReference.getClassReference());
                    return;
                }

                this.validateCommonComponent(constantReference, (PhpReference) constantReference.getClassReference());
            }

            @Override
            public void visitPhpFieldReference(@NotNull FieldReference fieldReference) {
                if (fieldReference.isStatic()) {
                    this.validateCommonComponent(fieldReference, (PhpReference) fieldReference.getClassReference());
                }
            }

            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (PREFER_CLASS_NAMES && "__CLASS__".equals(reference.getName())) {
                    final PhpClass clazz = PsiTreeUtil.getParentOfType(reference, PhpClass.class);
                    if (clazz != null && !clazz.isAnonymous() && !clazz.isTrait()) {
                        registerProblem(reference, clazz.getName() + "::class", "__CLASS__");
                    }
                }
            }

            private boolean isSameFQN(@NotNull final PsiElement psiElement, @Nullable final PhpReference reference) {
                if (reference == null) {
                    return false;
                }

                final String referenceName = reference.getName();
                if (PREFER_CLASS_NAMES) {
                    return "self".equals(referenceName);
                } else {
                    if ("self".equals(referenceName) || "static".equals(referenceName)) {
                        return false;
                    }

                    final PhpClass clazz = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
                    return clazz != null && clazz.getFQN().equals(reference.getFQN());
                }
            }

            @Nullable
            private String getClassName(@NotNull PhpReference psiElement) {
                if (!PREFER_CLASS_NAMES) {
                    return psiElement.getName();
                } else {
                    final PhpClass clazz = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
                    if (clazz != null && !clazz.isAnonymous()) {
                        return clazz.getName();
                    } else {
                        return null;
                    }
                }
            }

            private void validateClassConstantComponent(@NotNull final PsiElement constantReference, @Nullable final PhpReference classReference) {
                if (isSameFQN(constantReference, classReference)) {
                    if (!PREFER_CLASS_NAMES) {
                        final PhpClass clazz = PsiTreeUtil.getParentOfType(classReference, PhpClass.class);
                        if (clazz != null && !clazz.isAnonymous() && !clazz.isTrait()){
                            registerProblem(classReference.getParent(), getClassName(classReference) + "::class", "__CLASS__");
                        }
                    } else {
                        final String className          = getClassName(classReference);
                        final String classReferenceName = classReference.getName();
                        if (!StringUtils.isEmpty(className) && !StringUtils.isEmpty(classReferenceName)) {
                            this.registerProblem(classReference, className, classReferenceName);
                        }
                    }
                }
            }

            private void validateCommonComponent(@NotNull PsiElement expression, @Nullable PhpReference reference) {
                if (this.isSameFQN(expression, reference)) {
                    final String className = getClassName(reference);
                    if (!StringUtils.isEmpty(className)) {
                        this.registerProblem(reference, className, "self");
                    }
                }
            }

            private void registerProblem(@NotNull PsiElement target, @NotNull String className, @NotNull String replacement) {
                if (PREFER_CLASS_NAMES) {
                    problemsHolder.registerProblem(
                            target,
                            String.format(messagePattern, replacement, className),
                            new TheLocalFix(className)
                    );
                } else {
                    problemsHolder.registerProblem(
                            target,
                            String.format(messagePattern, className, replacement),
                            new TheLocalFix(replacement)
                    );
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Prefer class names", PREFER_CLASS_NAMES, (isSelected) -> PREFER_CLASS_NAMES = isSelected)
        );
    }

    private static final class TheLocalFix implements LocalQuickFix {
        @NotNull
        private final String replacement;

        private TheLocalFix(@NotNull final String replacementName) {
            this.replacement = replacementName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Replace with '" + replacement + "'";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null) {
                if (replacement.endsWith("::class")) {
                    final String pattern = this.replacement + ';';
                    final ClassConstantReference replacement
                            = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, pattern);
                    if (replacement != null) {
                        target.replace(replacement);
                    }
                } else {
                    target.replace(PhpPsiElementFactory.createClassReference(project, this.replacement));
                }
            }
        }
    }
}
