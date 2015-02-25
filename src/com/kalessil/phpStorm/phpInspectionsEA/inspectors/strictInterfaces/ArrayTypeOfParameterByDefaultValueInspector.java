package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayTypeOfParameterByDefaultValueInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Declare this parameter with 'array' type";

    @NotNull
    public String getShortName() {
        return ...;
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

            /**
             * @param callable to inspect
             */
            private void inspectCallable (Function callable) {
                for (Parameter objParameter : callable.getParameters()) {
                    if (this.canBePrependedWithArrayType(objParameter)) {
                        holder.registerProblem(objParameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            /**
             * @param parameter to inspect
             * @return boolean
             */
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