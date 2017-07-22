package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelfClassReferencingInspector extends BasePhpInspection {
    private static final String messageSelfReplacement  = "Class reference \"%s\" could be replaced by \"self\"";

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
                validateCommonComponent(constantReference, (PhpReference) constantReference.getClassReference());
            }

            @Override
            public void visitPhpFieldReference(final FieldReference fieldReference) {
                if (fieldReference.isStatic()) {
                    validateCommonComponent(fieldReference, (PhpReference) fieldReference.getClassReference());
                }
            }

            private boolean isSameFQN(@NotNull final PsiElement constantReference, @Nullable final PhpReference classReference) {
                if (classReference == null) {
                    return false;
                }

                final PhpClass expressionParentClass = PsiTreeUtil.getParentOfType(constantReference, PhpClass.class);

                return (expressionParentClass != null) &&
                       (expressionParentClass.getFQN().equals(classReference.getFQN()));
            }

            private void validateCommonComponent(final PsiElement expression, @Nullable final PhpReference classReference) {
                if (isSameFQN(expression, classReference)) {
                    problemsHolder.registerProblem(classReference, String.format(messageSelfReplacement, classReference.getName()));
                }
            }
        };
    }
}
