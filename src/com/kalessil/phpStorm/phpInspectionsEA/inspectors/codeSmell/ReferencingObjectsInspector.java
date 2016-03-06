package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ReferencingObjectsInspector extends BasePhpInspection {
    private static final String strProblemParameter  = "Objects are always passed by reference; please correct '& $%p%'";
    private static final String strProblemAssignment = "Objects are always passed by reference; please correct '= & new '";

    private static final PhpType php7Types;
    static {
        php7Types = (new PhpType())
            .add(PhpType.STRING)
            .add(PhpType.INT)
            .add(PhpType.FLOAT)
            .add(PhpType.BOOLEAN)
            .add(PhpType.ARRAY)
        ;
    }

    @NotNull
    public String getShortName() {
        return "ReferencingObjectsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.inspectCallable(method);
            }
            public void visitPhpFunction(Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                if (null == callable.getNameIdentifier()) {
                    return;
                }

                for (Parameter objParameter : callable.getParameters()) {
                    if (
                        objParameter.isPassByRef() && !objParameter.getDeclaredType().isEmpty() &&
                        !PhpType.isSubType(objParameter.getDeclaredType(), php7Types)
                    ) {
                        final String message = strProblemParameter.replace("%p%", objParameter.getName());
                        holder.registerProblem(objParameter, message, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            public void visitPhpNewExpression(NewExpression expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
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
