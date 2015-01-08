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
    private static final String strProblemDescription =
            "'isset(...)' returns true when key is present and associated with null value. " +
            "'array_key_exists(...)' construction can be used instead.";

    private static final String strProblemDescriptionUseNullComparison = "Use 'null !== $...' construction instead";

    private static final String strProblemDescriptionConcatenationInIndex = "Contains concatenation expression as index. " +
            "Define this index as variable.";


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

                    if (parameter instanceof ArrayAccessExpression) {
                        if (this.hasConcatenationAsIndex((ArrayAccessExpression) parameter)) {
                            holder.registerProblem(parameter, strProblemDescriptionConcatenationInIndex, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }

                        holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }

                    holder.registerProblem(parameter, strProblemDescriptionUseNullComparison, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            /** checks if any of indexes is concatenation expression */
            private boolean hasConcatenationAsIndex(ArrayAccessExpression objExpression) {
                ArrayIndex objIndex;
                PsiElement objIndexExpression;
                IElementType objOperationType;

                PsiElement objExpressionToInspect = objExpression;
                while (objExpressionToInspect instanceof ArrayAccessExpression) {
                    objIndex = ((ArrayAccessExpression) objExpressionToInspect).getIndex();

                    if (null != objIndex) {
                        objIndexExpression = objIndex.getValue();
                        if (objIndexExpression instanceof BinaryExpression) {
                            objOperationType = ((BinaryExpression) objIndexExpression).getOperation().getNode().getElementType();
                            if (objOperationType == PhpTokenTypes.opCONCAT) {
                                return true;
                            }
                        }
                    }

                    objExpressionToInspect =  objExpressionToInspect.getParent();
                }

                return false;
            }
        };
    }
}