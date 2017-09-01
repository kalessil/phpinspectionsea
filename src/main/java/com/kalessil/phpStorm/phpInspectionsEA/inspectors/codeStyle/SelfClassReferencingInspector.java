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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

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

    private static final Set<String> lateBinding = new HashSet<>();
    static {
        lateBinding.add("self");
        lateBinding.add("static");
    }

    @NotNull
    public String getShortName() {
        return "SelfClassReferencingInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            /* TODO: hook the method definition instead, search ClassReference do invocation smarter */

            @Override
            public void visitPhpClassConstantReference(@NotNull ClassConstantReference constantReference) {
                final String constantName = constantReference.getName();
                if (constantName != null &&  constantName.equals("class")) {
                    final PsiElement classReference = constantReference.getClassReference();
                    if (classReference instanceof ClassReference) {
                        this.analyzeClassConstant((ClassReference) classReference);
                    }
                } else {
                    this.analyzeMemberReference(constantReference);
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference methodReference) {
                this.analyzeMemberReference(methodReference);
            }

            @Override
            public void visitPhpFieldReference(@NotNull FieldReference fieldReference) {
                this.analyzeMemberReference(fieldReference);
            }

            @Override
            public void visitPhpNewExpression(@NotNull NewExpression newExpression) {
                final PsiElement classReference = newExpression.getClassReference();
                if (classReference != null) {
                    this.analyze((ClassReference) classReference);
                }
            }

            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference constantReference) {
                /* TODO: relocate into analyzeClassConstant */
                if (PREFER_CLASS_NAMES) {
                    final String constantName = constantReference.getName();
                    if (constantName != null && constantName.equals("__CLASS__")) {
                        final PhpClass clazz = PsiTreeUtil.getParentOfType(constantReference, PhpClass.class);
                        if (clazz != null && !clazz.isAnonymous() && !clazz.isTrait()) {
                            this.registerProblem(constantReference, clazz.getName() + "::class", "__CLASS__");
                        }
                    }
                }
            }

            private void analyzeMemberReference(@NotNull MemberReference reference) {
                if (reference.isStatic()) {
                    final PsiElement classReference = reference.getClassReference();
                    if (classReference instanceof ClassReference) {
                        this.analyze((ClassReference) classReference);
                    }
                }
            }

            private boolean isSameFQN(@NotNull ClassReference reference, @NotNull PhpClass clazz) {
                final String referenceName = reference.getName();
                if (PREFER_CLASS_NAMES) {
                    return referenceName != null && referenceName.equals("self");
                } else {
                    return !lateBinding.contains(referenceName) && clazz.getFQN().equals(reference.getFQN());
                }
            }

            private void analyzeClassConstant(@NotNull ClassReference reference) {
                final PhpClass clazz = PsiTreeUtil.getParentOfType(reference, PhpClass.class);
                if (clazz != null && this.isSameFQN(reference, clazz)) {
                    if (PREFER_CLASS_NAMES) {
                        final String className          = this.getClassName(reference, clazz);
                        final String classReferenceName = reference.getName();
                        if (!StringUtils.isEmpty(className) && !StringUtils.isEmpty(classReferenceName)) {
                            this.registerProblem(reference, className, classReferenceName);
                        }
                    } else {
                        if (!clazz.isAnonymous() && !clazz.isTrait()) {
                            this.registerProblem(reference.getParent(), this.getClassName(reference, clazz) + "::class", "__CLASS__");
                        }
                    }
                }
            }

            private void analyze(@NotNull ClassReference reference) {
                final PhpClass clazz = PsiTreeUtil.getParentOfType(reference, PhpClass.class);
                if (clazz != null && this.isSameFQN(reference, clazz)) {
                    final String className = this.getClassName(reference, clazz);
                    if (!StringUtils.isEmpty(className)) {
                        this.registerProblem(reference, className, "self");
                    }
                }
            }

            @Nullable
            private String getClassName(@NotNull ClassReference reference, @NotNull PhpClass clazz) {
                final String result;
                if (PREFER_CLASS_NAMES) {
                    result = clazz.isAnonymous() ? null : clazz.getName();
                } else {
                    result = reference.getName();
                }
                return result;
            }

            private void registerProblem(@NotNull PsiElement target, @NotNull String className, @NotNull String replacement) {
                final String from = PREFER_CLASS_NAMES ? replacement : className;
                final String to   = PREFER_CLASS_NAMES ? className : replacement;
                problemsHolder.registerProblem(target, String.format(messagePattern, from, to), new TheLocalFix(to));
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
            return "Apply configured class reference style";
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
