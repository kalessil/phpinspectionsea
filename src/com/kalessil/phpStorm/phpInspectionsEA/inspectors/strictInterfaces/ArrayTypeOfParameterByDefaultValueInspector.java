package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayTypeOfParameterByDefaultValueInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Parameter $%p% can be declared as 'array $%p%'";

    @NotNull
    public String getShortName() {
        return "ArrayTypeOfParameterByDefaultValueInspection";
    }

    @Override
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
                if (null == callable.getNameIdentifier()) {
                    return;
                }

                if (callable instanceof Method) {
                    PhpClass clazz = ((Method) callable).getContainingClass();
                    String methodName = callable.getName();
                    if (!StringUtil.isEmpty(methodName) && null != clazz && !clazz.isInterface()) {
                        /* ensure not reporting children classes, only parent definitions */
                        for (PhpClass parent : clazz.getSupers()) {
                            if (null != parent.findMethodByName(methodName)) {
                                return;
                            }
                        }
                    }
                }

                for (Parameter objParameter : callable.getParameters()) {
                    if (this.canBePrependedWithArrayType(objParameter)) {
                        String strWarning = strProblemDescription.replace("%p%", objParameter.getName());
                        holder.registerProblem(callable.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            public boolean canBePrependedWithArrayType(Parameter parameter) {
                if (!parameter.isOptional()) {
                    return false;
                }

                PsiElement objDefaultValue = parameter.getDefaultValue();
                if (!(objDefaultValue instanceof ArrayCreationExpression)) {
                    return false;
                }

                //noinspection RedundantIfStatement
                if (!parameter.getDeclaredType().isEmpty()) {
                    return false;
                }

                return true;
            }
        };
    }
}