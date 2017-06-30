package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
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
            /** re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.inspectCallable(method);
            }
            public void visitPhpFunction(Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (Function callable) {
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(callable);
                if (null == nameNode) {
                    return;
                }

                if (callable instanceof Method) {
                    /* the method can be as it is due to inheritance; skip reporting the case; */
                    final PhpClass clazz = ((Method) callable).getContainingClass();
                    if (null != clazz && !clazz.isInterface()) {
                        final String methodName = callable.getName();
                        for (PhpClass parent : clazz.getSupers()) {
                            if (null != parent && null != parent.findMethodByName(methodName)) {
                                return;
                            }
                        }
                    }
                }

                for (Parameter param : callable.getParameters()) {
                    if (this.canBePrependedWithArrayType(param)) {
                        final String message = messagePattern.replace("%p%", param.getName());
                        holder.registerProblem(nameNode, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(param));
                    }
                }
            }

            private boolean canBePrependedWithArrayType(@NotNull Parameter parameter) {
                return parameter.isOptional() &&
                        parameter.getDeclaredType().isEmpty() &&
                        parameter.getDefaultValue() instanceof ArrayCreationExpression;
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
            final Parameter param         = this.param.getElement();
            final PsiElement defaultValue = null == param ? null : param.getDefaultValue();
            if (null != defaultValue) {
                final String pattern = "array %r%$%n% = %d%"
                    .replace("%d%", defaultValue.getText())
                    .replace("%n%", param.getName())
                    .replace("%r%", param.isPassByRef() ? "&" : "");
                param.replace(PhpPsiElementFactory.createComplexParameter(project, pattern));
            }
        }
    }
}