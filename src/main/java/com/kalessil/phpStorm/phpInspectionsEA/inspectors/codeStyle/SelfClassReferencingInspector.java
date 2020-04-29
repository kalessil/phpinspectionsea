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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

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
    @Override
    public String getShortName() {
        return "SelfClassReferencingInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Self class referencing";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpClass clazz = method.getContainingClass();
                if (clazz != null && !clazz.isAnonymous() && !clazz.isTrait() && !method.isAbstract()) {
                    final String targetReference   = PREFER_CLASS_NAMES ? "self" : clazz.getName();
                    final String targetReplacement = PREFER_CLASS_NAMES ? clazz.getName() : "self";
                    final GroupStatement body      = ExpressionSemanticUtil.getGroupStatement(method);

                    PsiTreeUtil.findChildrenOfType(body, ClassReference.class).stream()
                            .filter(reference  ->
                                targetReference.equals(reference.getName()) &&
                                method == PsiTreeUtil.getParentOfType(reference, Function.class) &&
                                clazz  == OpenapiResolveUtil.resolveReference(reference)
                            )
                            .forEach(reference -> {
                                final PsiElement parent = reference.getParent();

                                if (!PREFER_CLASS_NAMES && parent instanceof ClassConstantReference) {
                                    final String constantName = ((ClassConstantReference) parent).getName();
                                    if (constantName != null && constantName.equals("class")) {
                                        final String replacement = "__CLASS__";
                                        problemsHolder.registerProblem(
                                                parent,
                                                MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, parent.getText(), replacement)),
                                                new NormalizeReferenceFix(replacement)
                                        );
                                        return;
                                    }
                                }

                                if (!(parent instanceof ExtendsList)) {
                                    problemsHolder.registerProblem(
                                            reference,
                                            MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, targetReference, targetReplacement)),
                                            new NormalizeReferenceFix(targetReplacement)
                                    );
                                }
                            });

                    if (PREFER_CLASS_NAMES) {
                        PsiTreeUtil.findChildrenOfType(body, ConstantReference.class).stream()
                                .filter(reference  ->
                                    "__CLASS__".equals(reference.getName()) &&
                                    method == PsiTreeUtil.getParentOfType(reference, Function.class)
                                )
                                .forEach(reference -> {
                                    final String replacement = targetReplacement + "::class";
                                    problemsHolder.registerProblem(
                                            reference,
                                            MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, reference.getText(), replacement)),
                                            new NormalizeReferenceFix(replacement)
                                    );
                                });
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Prefer class names", PREFER_CLASS_NAMES, (isSelected) -> PREFER_CLASS_NAMES = isSelected)
        );
    }

    private static final class NormalizeReferenceFix implements LocalQuickFix {
        private static final String title = "Apply configured class reference style";

        private final String replacement;

        private NormalizeReferenceFix(@NotNull final String replacementName) {
            this.replacement = replacementName;
        }

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
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                if (replacement.endsWith("::class")) {
                    final PsiElement replacement
                            = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, this.replacement + ';');
                    if (replacement != null) {
                        target.replace(replacement);
                    }
                } else if (replacement.equals("__CLASS__")) {
                    target.replace(PhpPsiElementFactory.createConstantReference(project, this.replacement));
                } else {
                    target.replace(PhpPsiElementFactory.createClassReference(project, this.replacement));
                }
            }
        }
    }
}
