package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

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
    private static final String strProblemDescription = "Can be simplified by replacing this with following " +
            "return with one return statement";

    @NotNull
    public String getDisplayName() {
        return "Semantics: if-return-return simplification";
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

                /** or condition is not an binary expression */
                final PsiElement objCondition = ifStatement.getCondition();
                final boolean isBinaryExpressionInCondition = (
                    objCondition instanceof BinaryExpression || (
                        objCondition instanceof ParenthesizedExpression &&
                        ((ParenthesizedExpression) objCondition).getArgument() instanceof BinaryExpression
                    )
                    /** or maybe try resolving type when not on-the-fly analysis is running */
                );
                if (!isBinaryExpressionInCondition) {
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
                    objSecondReturn.getArgument() instanceof ConstantReference && (
                        PhpLangUtil.isTrue((ConstantReference) objSecondReturn.getArgument()) ||
                        PhpLangUtil.isFalse((ConstantReference) objSecondReturn.getArgument())
                    )
                );
                if (!isSecondReturnUsesBool) {
                    return;
                }

                /** Skip ifs without curvy brackets */
                GroupStatement objIfBody = null;
                for (PsiElement objIfChild : ifStatement.getChildren()) {
                    if (objIfChild instanceof GroupStatement) {
                        objIfBody = (GroupStatement) objIfChild;
                        break;
                    }
                }
                if (null == objIfBody) {
                    return;
                }
                /** analyse if structure contains only return inside */
                int countExpressionInIf = 0;
                PhpReturn objFirstReturn = null;
                for (PsiElement objIfChild : objIfBody.getChildren()) {
                    if (!(objIfChild instanceof PhpPsiElement)) {
                        continue;
                    }

                    ++countExpressionInIf;
                    if (objIfChild instanceof PhpReturn) {
                        objFirstReturn = (PhpReturn) objIfChild;
                    }
                }
                if (countExpressionInIf != 1 || null == objFirstReturn) {
                    return;
                }


                /** check if first return also boolean */
                final boolean isFirstReturnUsesBool = (
                    objFirstReturn.getArgument() instanceof ConstantReference && (
                        PhpLangUtil.isTrue((ConstantReference) objFirstReturn.getArgument()) ||
                        PhpLangUtil.isFalse((ConstantReference) objFirstReturn.getArgument())
                    )
                );
                if (!isFirstReturnUsesBool) {
                    return;
                }


                holder.registerProblem(ifStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}