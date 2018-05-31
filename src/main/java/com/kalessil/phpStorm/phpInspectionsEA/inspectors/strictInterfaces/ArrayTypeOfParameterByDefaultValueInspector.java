package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayTypeOfParameterByDefaultValueInspector extends BasePhpInspection {
    private static final String messagePattern = "Parameter $%p% can be declared as 'array $%p%'.";

    @NotNull
    public String getShortName() {
        return "ArrayTypeOfParameterByDefaultValueInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectCallable(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(callable);
                if (nameNode != null) {
                    /* false-positives: overridden methods */
                    if (callable instanceof Method) {
                        final PhpClass clazz = ((Method) callable).getContainingClass();
                        if (clazz != null && !clazz.isInterface()) {
                            final String methodName      = callable.getName();
                            final List<PhpClass> parents = OpenapiResolveUtil.resolveImplementedInterfaces(clazz);
                            parents.add(OpenapiResolveUtil.resolveSuperClass(clazz));
                            for (final PhpClass parent : parents) {
                                if (parent != null && OpenapiResolveUtil.resolveMethod(parent, methodName) != null) {
                                    return;
                                }
                            }
                        }
                    }

                    Arrays.stream(callable.getParameters())
                        .filter(this::canBePrependedWithArrayType)
                        .forEach(param -> {
                            final String message = messagePattern.replace("%p%", param.getName());
                            holder.registerProblem(nameNode, message, new TheLocalFix(param));
                        });
                }
            }

            private boolean canBePrependedWithArrayType(@NotNull Parameter parameter) {
                boolean result = false;
                if (parameter.getDefaultValue() instanceof ArrayCreationExpression && parameter.getDeclaredType().isEmpty()) {
                    final PhpType resolved = OpenapiResolveUtil.resolveType(parameter, parameter.getProject());
                    result = resolved == null || resolved.size() == 1;
                }
                return result;
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<Parameter> param;

        TheLocalFix(@NotNull Parameter param) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(param.getProject());

            this.param = factory.createSmartPsiElementPointer(param);
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare as array";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final Parameter param = this.param.getElement();
            if (param != null && !project.isDisposed()) {
                param.replace(PhpPsiElementFactory.createComplexParameter(project, "array " + param.getText()));
            }
        }
    }
}