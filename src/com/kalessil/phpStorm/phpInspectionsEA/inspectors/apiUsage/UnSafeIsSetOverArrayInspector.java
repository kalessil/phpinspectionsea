package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Probably 'array_key_exists(...)' construction should be used for better data *structure* control";
    private static final String strProblemDescriptionUseNullComparison = "Probably it should be 'null === $...' construction used";
    private static final String strProblemDescriptionConcatenationInIndex = "Concatenation is used as an index, should be moved to a variable";

    @NotNull
    public String getDisplayName() {
        return "Control flow: 'isset(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "UnSafeIsSetOverArrayInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIsset(PhpIsset issetExpression) {
                for (PsiElement parameter : issetExpression.getVariables()) {
                    parameter = ExpressionSemanticUtil.getExpressionTroughParenthesis(parameter);
                    if (null == parameter) {
                        continue;
                    }

                    if (!(parameter instanceof ArrayAccessExpression)) {
                        holder.registerProblem(parameter, strProblemDescriptionUseNullComparison, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        continue;
                    }

                    if (this.hasConcatenationAsIndex((ArrayAccessExpression) parameter)) {
                        holder.registerProblem(parameter, strProblemDescriptionConcatenationInIndex, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        continue;
                    }

                    holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }

            /** checks if any of indexes is concatenation expression */
            /** TODO: iterator for array access expression */
            private boolean hasConcatenationAsIndex(ArrayAccessExpression objExpression) {
                PsiElement objExpressionToInspect = objExpression;
                while (objExpressionToInspect instanceof ArrayAccessExpression) {
                    ArrayIndex objIndex = ((ArrayAccessExpression) objExpressionToInspect).getIndex();
                    if (null != objIndex && objIndex.getValue() instanceof BinaryExpression) {
                        PsiElement objOperation = ((BinaryExpression) objIndex.getValue()).getOperation();
                        if (null != objOperation && objOperation.getNode().getElementType() == PhpTokenTypes.opCONCAT) {
                            return true;
                        }
                    }

                    objExpressionToInspect =  objExpressionToInspect.getParent();
                }

                return false;
            }
        };
    }
}