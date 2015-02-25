package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfConditionalsWithoutGroupStatementInspector extends BasePhpInspection {
    private static final String strProblemMissingBrackets = "Wrap the conditional body with group statement";
    private static final String strProblemEmptyBody = "Empty group statement";

    @NotNull
    public String getShortName() {
        return ...;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                this.checkBrackets(ifStatement);
            }

            public void visitPhpElseIf(ElseIf elseIfStatement) {
                this.checkBrackets(elseIfStatement);
            }

            public void visitPhpElse(Else elseStatement) {
                this.checkBrackets(elseStatement);
            }

            private void checkBrackets(PhpPsiElement objConditional) {
                GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(objConditional);
                if (null != objGroupStatement) {
                    if (ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement) == 0) {
                        holder.registerProblem(objConditional.getFirstChild(), strProblemEmptyBody, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }

                    return;
                }

                holder.registerProblem(objConditional.getFirstChild(), strProblemMissingBrackets, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
