package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MockingMethodsCorrectnessInspector extends BasePhpInspection {
    private final static String messageWillMethod = "It probably was intended to use '->will(...)' here.";

    @NotNull
    public String getShortName() {
        return "MockingMethodsCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* TODO: ->getMockBuilder(::class)->getMock() + (->expect())->method('non-existing') */

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null && methodName.equals("willReturn")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && arguments[0] instanceof MethodReference) {
                        final String innerMethodName = ((MethodReference) arguments[0]).getName();
                        if (innerMethodName != null) {
                            final boolean isTarget
                                    = innerMethodName.equals("returnCallback") || innerMethodName.equals("returnValue");
                            if (isTarget) {
                                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(reference);
                                if (nameNode != null && this.isTestContext(reference)) {
                                    holder.registerProblem(nameNode, messageWillMethod, new UseWillMethodFix());
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static class UseWillMethodFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use '->will(...)' instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement().getParent();
            if (target instanceof FunctionReference && !project.isDisposed()) {
                ((FunctionReference) target).handleElementRename("will");
            }
        }
    }
}
