package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ParameterByRefWithDefaultInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Usually default value is not needed in this case";

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
                    if (objParameter.isPassByRef() && objParameter.isOptional()) {
                        holder.registerProblem(objParameter, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}