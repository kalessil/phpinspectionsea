package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Try;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class BadExceptionsProcessingInspector extends BasePhpInspection {    
    private static final String strProblemDedicateLogic = "Consider moving non-related statements outside the try-block or refactoring try-body into a function/method.";

    @NotNull
    public String getShortName() {
        return "BadExceptionsProcessingInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTry(Try tryStatement) {
                GroupStatement body = ExpressionSemanticUtil.getGroupStatement(tryStatement);
                if (null != body && ExpressionSemanticUtil.countExpressionsInGroup(body) > 2) {
                    //noinspection ConstantConditions
                    holder.registerProblem(tryStatement.getFirstChild(), strProblemDedicateLogic, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
