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

public class UselessReturnInspector extends BasePhpInspection {
    private static final String strProblemUseless   = "Senseless statement: safely remove it";
    private static final String strProblemConfusing = "Confusing statement: shall be re-factored";

    @NotNull
    public String getShortName() {
        return "UselessReturnInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpReturn(PhpReturn returnStatement) {
                PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue(returnStatement);
                if (returnValue instanceof AssignmentExpression && ((AssignmentExpression) returnValue).getVariable() instanceof Variable) {
                    holder.registerProblem(returnStatement, strProblemConfusing, ProblemHighlightType.WEAK_WARNING);
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

