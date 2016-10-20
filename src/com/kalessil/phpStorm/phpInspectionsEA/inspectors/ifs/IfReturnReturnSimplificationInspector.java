package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String messagePattern = "If and following return can be replaced with 'return %c%'";

    @NotNull
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /* skip ifs with alternative branches */
                if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }


                /* Skip ifs without group statement */
                final GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (null == objGroupStatement) {
                    return;
                }


                /* or condition is not an binary expression */
                final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifStatement.getCondition());
                /* or maybe try resolving type when not on-the-fly analysis is running */
                /* TODO: function/method/property/constant reference, ternary and etc? Do we check not null?  */
                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }


                /* next expression is not return */
                final PsiElement objNextExpression = ifStatement.getNextPsiSibling();
                if (!(objNextExpression instanceof PhpReturn)) {
                    return;
                }


                /* when if has preceding if-return */
                final PsiElement previousExpression = ifStatement.getPrevPsiSibling();
                if (previousExpression instanceof If && !ExpressionSemanticUtil.hasAlternativeBranches((If) previousExpression)) {
                    final GroupStatement previousIfBody = ExpressionSemanticUtil.getGroupStatement(previousExpression);
                    if (null != previousIfBody) {
                        final PsiElement previousReturnCandidate = ExpressionSemanticUtil.getLastStatement(previousIfBody);
                        if (previousReturnCandidate instanceof PhpReturn) {
                            return;
                        }
                    }
                }


                /* or return not a boolean */
                final PhpReturn objSecondReturn = (PhpReturn) objNextExpression;
                final boolean isSecondReturnUsesBool = (
                    objSecondReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondReturn.getArgument())
                );
                if (!isSecondReturnUsesBool) {
                    return;
                }


                /* analyse if structure contains only one expression */
                final int intCountExpressionsInCurrentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement);
                if (intCountExpressionsInCurrentGroup != 1) {
                    return;
                }
                /* and it's a return expression */
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


                /* check if first return also boolean */
                final boolean isFirstReturnUsesBool = (
                    objFirstReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) objFirstReturn.getArgument())
                );
                if (!isFirstReturnUsesBool) {
                    return;
                }


                /* point the problem out */
                final boolean isInverted = PhpLangUtil.isFalse((ConstantReference) objFirstReturn.getArgument());
                String message = isInverted ? messagePattern.replace("%c%", "!(%c%)") : messagePattern;

                message = message.replace("%c%", ifStatement.getCondition().getText());
                holder.registerProblem(ifStatement.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }

    // TODO: quick fixing: double double inversion must be handled properly
}