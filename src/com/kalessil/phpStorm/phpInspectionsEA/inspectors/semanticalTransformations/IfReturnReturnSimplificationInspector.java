package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Can be simplified by replacing this with following " +
            "return with one return statement";

    @NotNull
    public String getDisplayName() {
        return "Control flow: if-return-return simplification";
    }

    @NotNull
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /** skip ifs with alternative branches */
                if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }


                /** Skip ifs without group statement */
                GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (null == objGroupStatement) {
                    return;
                }


                /** or condition is not an binary expression */
                final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifStatement.getCondition());
                /** or maybe try resolving type when not on-the-fly analysis is running */
                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }


                /** next expression is not return */
                PhpPsiElement objNextExpression = ifStatement.getNextPsiSibling();
                if (!(objNextExpression instanceof PhpReturn)) {
                    return;
                }

                /** or return not a boolean */
                PhpReturn objSecondReturn = (PhpReturn) objNextExpression;
                final boolean isSecondReturnUsesBool = (
                    objSecondReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondReturn.getArgument())
                );
                if (!isSecondReturnUsesBool) {
                    return;
                }


                /** analyse if structure contains only one expression */
                int intCountExpressionsInCurrentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement);
                if (intCountExpressionsInCurrentGroup != 1) {
                    return;
                }
                /** and it's a return expression */
                PhpReturn objFirstReturn = null;
                for (PsiElement objIfChild : objGroupStatement.getChildren()) {
                    if (objIfChild instanceof PhpReturn) {
                        objFirstReturn = (PhpReturn) objIfChild;
                        break;
                    }
                }
                if (null == objFirstReturn) {
                    return;
                }


                /** check if first return also boolean */
                final boolean isFirstReturnUsesBool = (
                    objFirstReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) objFirstReturn.getArgument())
                );
                if (!isFirstReturnUsesBool) {
                    return;
                }


                /** point the problem out */
                holder.registerProblem(ifStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}