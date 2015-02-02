package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class UselessReturnInspector extends BasePhpInspection {
    private static final String strProblemUselessReturnValue = "%s% method shall not return any value";
    private static final String strProblemUseless = "Senseless statement: safely remove it";
    private static final String strProblemConfusing = "Confusing statement: shall be re-factored";

    private HashSet<String> methodsSet = null;
    private HashSet<String> getMethodsSet() {
        if (null == methodsSet) {
            methodsSet = new HashSet<String>();

            methodsSet.add("__construct");
            methodsSet.add("__destruct");
            methodsSet.add("__set");
            methodsSet.add("__clone");
            methodsSet.add("__unset");
        }

        return methodsSet;
    }

    @NotNull
    public String getDisplayName() {
        return "Confusing constructs: useless return";
    }

    @NotNull
    public String getShortName() {
        return "UselessReturnInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpReturn(PhpReturn returnStatement) {
                PhpExpression objReturnValue = ExpressionSemanticUtil.getReturnValue(returnStatement);
                if (null == objReturnValue) {
                    return;
                }

                if (
                    objReturnValue instanceof AssignmentExpression &&
                    ((AssignmentExpression) objReturnValue).getVariable() instanceof Variable
                ) {
                    holder.registerProblem(returnStatement, strProblemConfusing, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                Function objScope = ExpressionSemanticUtil.getScope(returnStatement);
                if (objScope instanceof Method) {
                    String strMethodName = objScope.getName();
                    if (getMethodsSet().contains(strMethodName)) {
                        String strMessage = strProblemUselessReturnValue.replace("%s%", strMethodName);
                        holder.registerProblem(objReturnValue, strMessage, ProblemHighlightType.ERROR);
                    }
                }
            }

            public void visitPhpMethod(Method method) {
                this.inspectForSenselessReturn(method);
            }

            public void visitPhpFunction(Function function) {
                this.inspectForSenselessReturn(function);
            }

            private void inspectForSenselessReturn(Function callable) {
                GroupStatement objBody = ExpressionSemanticUtil.getGroupStatement(callable);
                if (null == objBody) {
                    return;
                }

                PsiElement objLastExpression = ExpressionSemanticUtil.getLastStatement(objBody);
                if (!(objLastExpression instanceof PhpReturn)) {
                    return;
                }

                PhpExpression objReturnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) objLastExpression);
                if (null == objReturnValue) {
                    holder.registerProblem(objLastExpression, strProblemUseless, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
        };
    }
}

