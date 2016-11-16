package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayTypeOfParameterByDefaultValueInspector extends BasePhpInspection {
    private static final String messagePattern = "Parameter $%p% can be declared as 'array $%p%'";

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
                final PsiElement nameNode = callable.getNameIdentifier();
                if (null == nameNode || 0 == nameNode.getTextLength()) {
                    return;
                }

                if (callable instanceof Method) {
                    final PhpClass clazz    = ((Method) callable).getContainingClass();
                    final String methodName = callable.getName();
                    if (null != clazz && !clazz.isInterface() && !StringUtil.isEmpty(methodName)) {
                        /* ensure not reporting children classes, only parent definitions */
                        for (PhpClass parent : clazz.getSupers()) {
                            if (null != parent.findMethodByName(methodName)) {
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
        Parameter param;

        TheLocalFix(@NotNull Parameter param) {
            super();
            this.param = param;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use array type";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final String pattern = "array $%n% = array()".replace("%n%", this.param.getName());
            //noinspection ConstantConditions I'm sure NPE will not happen as pattern is hardcoded
            param.replace(PhpPsiElementFactory.createComplexParameter(project, pattern));

            /* Release node reference */
            this.param = null;
        }
    }
}