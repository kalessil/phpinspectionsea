package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MockingMethodsCorrectnessInspector extends BasePhpInspection {
    private final static String messageWillMethod       = "It probably was intended to use '->will(...)' here.";
    private final static String messageUnresolvedMethod = "The method was not resolved, perhaps it doesn't exist.";
    private final static String messageFinalMethod      = "The method is final hence can not be mocked.";

    @NotNull
    public String getShortName() {
        return "MockingMethodsCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null) {
                    if (methodName.equals("willReturn")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1 && arguments[0] instanceof MethodReference) {
                            final String innerMethodName = ((MethodReference) arguments[0]).getName();
                            if (innerMethodName != null) {
                                final boolean isTarget = innerMethodName.equals("returnCallback") ||
                                                         innerMethodName.equals("returnValue");
                                if (isTarget && this.isTestContext(reference)) {
                                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(reference);
                                    if (nameNode != null) {
                                        holder.registerProblem(nameNode, messageWillMethod, new UseWillMethodFix());
                                    }
                                }
                            }
                        }
                    } else if (methodName.equals("method")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1 && arguments[0] instanceof StringLiteralExpression) {
                            PhpPsiElement mock = reference.getFirstPsiChild();
                            /* Handle following construct (->expects())->method('non-existing') */
                            if (mock instanceof MethodReference && "expects".equals(mock.getName())) {
                                mock = mock.getFirstPsiChild();
                            }
                            if (mock != null && this.isTestContext(reference)) {
                                this.checkIfMockHasMethod(mock, (StringLiteralExpression) arguments[0]);
                            }
                        }
                    }
                }
            }

            private void checkIfMockHasMethod(@NotNull PsiElement mock, @NotNull StringLiteralExpression methodName) {
                final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(mock);
                if (variants.size() == 1) {
                    /* Handle following construct ->getMockBuilder(::class)->getMock() +  */
                    final PsiElement source = variants.iterator().next();
                    if (source instanceof MethodReference && "getMock".equals(((MethodReference) source).getName())) {
                        final Optional<MethodReference> builder
                                = PsiTreeUtil.findChildrenOfType(source, MethodReference.class).stream()
                                    .filter(reference -> "getMockBuilder".equals(reference.getName()))
                                    .findFirst();
                        if (builder.isPresent()) {
                            final PsiElement[] builderArguments = builder.get().getParameters();
                            if (builderArguments.length == 1 && builderArguments[0] instanceof ClassConstantReference) {
                                final ClassConstantReference clazz = (ClassConstantReference) builderArguments[0];
                                if ("class".equals(clazz.getName())) {
                                    final PsiElement classReference = clazz.getClassReference();
                                    if (classReference instanceof ClassReference) {
                                        final PsiElement resolved
                                                = OpenapiResolveUtil.resolveReference((ClassReference) classReference);
                                        if (resolved instanceof PhpClass) {
                                            final Method method = OpenapiResolveUtil.resolveMethod((PhpClass) resolved, methodName.getContents());
                                            if (method == null) {
                                                holder.registerProblem(methodName, messageUnresolvedMethod, ProblemHighlightType.GENERIC_ERROR);
                                            } else if (method.isFinal()) {
                                                holder.registerProblem(methodName, messageFinalMethod, ProblemHighlightType.GENERIC_ERROR);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                variants.clear();
            }
        };
    }

    private static final class UseWillMethodFix implements LocalQuickFix {
        private static final String title = "Use '->will(...)' instead";

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
            final PsiElement target = descriptor.getPsiElement().getParent();
            if (target instanceof FunctionReference && !project.isDisposed()) {
                ((FunctionReference) target).handleElementRename("will");
            }
        }
    }
}
