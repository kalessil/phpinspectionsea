package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ReferencingObjectsInspector extends BasePhpInspection {
    private static final String strProblemParameter = "Objects are always passed by reference, please correct '& $%p%'";
    private static final String strProblemAssignment = "Objects are always passed by reference, please correct '= & new '";

    @NotNull
    public String getShortName() {
        return "ReferencingObjectsInspection";
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

                for (Parameter objParameter : callable.getParameters()) {
                    /** TODO: php 7 scalar types */
                    if (objParameter.isPassByRef() && !objParameter.getDeclaredType() .isEmpty() && !PhpType.isSubType(objParameter.getDeclaredType(), PhpType.ARRAY)) {
                        String strWarning = strProblemParameter.replace("%p%", objParameter.getName());
                        holder.registerProblem(callable.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            public void visitPhpNewExpression(NewExpression expression) {
                PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() == expression) {
                        PsiElement operation = assignment.getValue().getPrevSibling();
                        if (operation instanceof PsiWhiteSpace) {
                            operation = operation.getPrevSibling();
                        }
                        if (null != operation && operation.getText().replaceAll("\\s+","").equals("=&")) {
                            holder.registerProblem(expression, strProblemAssignment, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }
}
