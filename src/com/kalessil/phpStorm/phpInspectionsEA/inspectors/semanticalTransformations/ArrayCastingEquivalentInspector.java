package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;


import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class ArrayCastingEquivalentInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Can be safely replaced with '(array) ...' construction";

    @NotNull
    public String getDisplayName() {
        return "Performance: array casting equivalent construction";
    }

    @NotNull
    public String getShortName() {
        return "ArrayCastingEquivalentInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If expression) {
                /** has alternative branches, skip it */
                if (expression.getElseIfBranches().length > 0 || null != expression.getElseBranch()){
                    return;
                }

                /** body has only assignment */
                GroupStatement objBody = ExpressionSemanticUtil.getGroupStatement(expression);
                if (null == objBody || ExpressionSemanticUtil.countExpressionsInGroup(objBody) != 1) {
                    return;
                }

                /** expecting !function(...) in condition */
                PsiElement objConditionExpression = null;
                IElementType objOperation = null;
                if (expression.getCondition() instanceof UnaryExpression) {
                    UnaryExpression objCondition = (UnaryExpression) expression.getCondition();
                    objOperation = objCondition.getOperation().getNode().getElementType();
                    objConditionExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objCondition.getValue());
                }
                if (objOperation != PhpTokenTypes.opNOT || !(objConditionExpression instanceof FunctionReference)) {
                    return;
                }

                /** expecting assignment in body */
                PsiElement objAction = ExpressionSemanticUtil.getLastStatement(objBody);
                objAction = (null == objAction ? null : objAction.getFirstChild());
                if (!(objAction instanceof AssignmentExpression)) {
                    return;
                }


                AssignmentExpression objAssignment = (AssignmentExpression) objAction;
                PsiElement objTrueVariant = objAssignment.getVariable();
                PsiElement objFalseVariant = objAssignment.getValue();
                if (null == objTrueVariant || null == objFalseVariant) {
                    return;
                }

                /** analyse valuable part */
                if (this.isArrayCasting(objConditionExpression, objTrueVariant, objFalseVariant)) {
                    holder.registerProblem(expression.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            public void visitPhpTernaryExpression(TernaryExpression expression) {
                /** expecting !function(...) in condition */
                PsiElement objConditionExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (!(objConditionExpression instanceof FunctionReference)) {
                    return;
                }

                PsiElement objTrueVariant = expression.getTrueVariant();
                PsiElement objFalseVariant = expression.getFalseVariant();
                if (null == objTrueVariant || null == objFalseVariant) {
                    return;
                }

                /** analyse valuable parts */
                if (this.isArrayCasting(objConditionExpression, objTrueVariant, objFalseVariant)) {
                    holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            private boolean isArrayCasting(@NotNull PsiElement objCondition, @NotNull PsiElement objTrue, @NotNull PsiElement objFalse) {
                /** false variant should be array creation */
                if (!(objFalse instanceof ArrayCreationExpression)) {
                    return false;
                }

                /** condition expected to be is_array(arg) */
                FunctionReference objConditionExpression = (FunctionReference) objCondition;
                String strFunctionName = objConditionExpression.getName();
                if (
                    objConditionExpression.getParameters().length != 1 ||
                    StringUtil.isEmpty(strFunctionName) ||
                    !strFunctionName.equals("is_array")
                ) {
                    return false;
                }

                /** extract array values, expected one value only */
                LinkedList<PsiElement> valuesSet = new LinkedList<>();
                for (PsiElement objChild : objFalse.getChildren()) {
                    if (objChild instanceof PhpPsiElement) {
                        valuesSet.add(objChild.getFirstChild());
                    }
                }
                //noinspection SimplifiableIfStatement
                if (valuesSet.size() != 1) {
                    return false;
                }

                return
                    PsiEquivalenceUtil.areElementsEquivalent(objTrue, objConditionExpression.getParameters()[0]) &&
                    PsiEquivalenceUtil.areElementsEquivalent(objTrue, valuesSet.getFirst());
            }
        };
    }
}
