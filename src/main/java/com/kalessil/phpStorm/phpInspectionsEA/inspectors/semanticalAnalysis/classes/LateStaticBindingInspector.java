package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LateStaticBindingInspector extends PhpInspection {
    private static final String messagePrivateMethod = "It's better to use 'self' here (identically named private method in child classes will cause an error).";

    @NotNull
    @Override
    public String getShortName() {
        return "LateStaticBindingInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Late static binding usage correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String methodName = reference.getName();
                if (methodName != null && !methodName.isEmpty()) {
                    final PsiElement base = reference.getClassReference();
                    if (base instanceof ClassReference && base.getText().equals("static")) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved instanceof Method && ((Method) resolved).getAccess().isPrivate()) {
                            final Function scope = ExpressionSemanticUtil.getScope(reference);
                            if (scope instanceof Method) {
                                final PhpClass clazz = ((Method) scope).getContainingClass();
                                if (clazz != null && !clazz.isFinal()) {
                                    holder.registerProblem(
                                            base,
                                            MessagesPresentationUtil.prefixWithEa(messagePrivateMethod),
                                            new UseSelfFix()
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseSelfFix implements LocalQuickFix {
        private static final String title = "Use 'self' instead";

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
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                target.replace(PhpPsiElementFactory.createClassReference(project, "self"));
            }
        }
    }
}
