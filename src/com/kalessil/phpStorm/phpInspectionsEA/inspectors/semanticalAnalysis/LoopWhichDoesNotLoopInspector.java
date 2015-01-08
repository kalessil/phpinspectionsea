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

public class LoopWhichDoesNotLoopInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This loop does not loop";

    @NotNull
    public String getDisplayName() {
        return "Control flow: loop which does not loop";
    }

    @NotNull
    public String getShortName() {
        return "LoopWhichDoesNotLoopInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                this.inspectBody(foreach);
            }
            public void visitPhpFor(For forStatement) {
                this.inspectBody(forStatement);
            }
            public void visitPhpWhile(While whileStatement) {
                this.inspectBody(whileStatement);
            }

            private void inspectBody(PhpPsiElement objLoop) {
                GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(objLoop);
                if (null == objGroupStatement) {
                    return;
                }

                PsiElement objLastExpression = ExpressionSemanticUtil.getLastStatement(objGroupStatement);
                boolean isLoopTerminatedWithLastExpression = (
                    objLastExpression instanceof PhpBreak ||
                    objLastExpression instanceof PhpReturn ||
                    objLastExpression instanceof PhpThrow
                );

                /** loop is empty or terminates on first iteration */
                if (null != objLastExpression && !isLoopTerminatedWithLastExpression) {
                    return;
                }

                holder.registerProblem(objLoop.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
