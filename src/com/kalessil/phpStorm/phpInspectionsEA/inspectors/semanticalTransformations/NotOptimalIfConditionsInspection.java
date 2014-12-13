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

import java.util.LinkedList;

public class NotOptimalIfConditionsInspection extends BasePhpInspection {
    private static final String strProblemDescription = "This operation execution costs less than at previous one";

    @NotNull
    public String getDisplayName() {
        return "Semantics: not optimal order in if conditions";
    }

    @NotNull
    public String getShortName() {
        return "NotOptimalIfConditionsInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                PsiElement objCondition = ifStatement.getCondition();
                if (null != objCondition) {
                    objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(objCondition);
                }
                if (objCondition instanceof UnaryExpression) {
                    objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(
                        ((UnaryExpression) objCondition).getValue()
                    );
                }
                if (null == objCondition) {
                    return;
                }


                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }


                this.analyseBinaryExpression((BinaryExpression) objCondition);
            }

            private void analyseBinaryExpression(BinaryExpression objTarget) {
                PsiElement objOperation = objTarget.getOperation();
                if (null == objOperation) {
                    return;
                }

                String strOperation = objOperation.getText();
                if (
                    !strOperation.equals("&&") &&
                    !strOperation.equals("||")
                ) {
                    return;
                }

                LinkedList<PsiElement> objPartsCollection = this.extractConditionParts(objTarget, strOperation);

                int intLoopCurrentCost;
                int intPreviousCost = 0;
                for (PsiElement objCond : objPartsCollection) {
                    intLoopCurrentCost = this.getExpressionCost(objCond);

                    if (intLoopCurrentCost < intPreviousCost) {
                        holder.registerProblem(objCond, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }

                    intPreviousCost = intLoopCurrentCost;
                }
                objPartsCollection.clear();
            }

            /**
             * @param objExpression to estimate for execution cost
             * @return costs
             */
            private int getExpressionCost(PsiElement objExpression) {
                objExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objExpression);

                if (objExpression instanceof ConstantReference || null == objExpression) {
                    return 0;
                }

                if (
                    objExpression instanceof ClassConstantReference ||
                    objExpression instanceof Variable
                ) {
                    return 1;
                }

                if (objExpression instanceof ArrayAccessExpression) {
                    return 2;
                }

                if (
                    objExpression instanceof MethodReference ||
                    objExpression instanceof FunctionReference
                ) {
                    return 10;
                }


                if (objExpression instanceof UnaryExpression) {
                    return this.getExpressionCost(((UnaryExpression) objExpression).getValue());
                }


                if (objExpression instanceof BinaryExpression) {
                    return
                            this.getExpressionCost(((BinaryExpression) objExpression).getRightOperand()) +
                            this.getExpressionCost(((BinaryExpression) objExpression).getLeftOperand());
                }

                return 10;
            }

            /**
             * @param objTarget expression for extracting sub-conditions
             * @param strOperation operator to take in consideration
             * @return list of sub-conditions in native order
             */
            private LinkedList<PsiElement> extractConditionParts(BinaryExpression objTarget, String strOperation) {
                LinkedList<PsiElement> objPartsCollection = new LinkedList<>();

                objPartsCollection.add(ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getRightOperand()));
                PsiElement objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(
                        objTarget.getLeftOperand()
                );
                while (
                    objExpressionToExpand instanceof BinaryExpression &&
                    ((BinaryExpression) objExpressionToExpand).getOperation() != null &&
                    ((BinaryExpression) objExpressionToExpand).getOperation().getText().equals(strOperation)
                ) {
                    objPartsCollection.addFirst(
                            ExpressionSemanticUtil.getExpressionTroughParenthesis(
                                    ((BinaryExpression) objExpressionToExpand).getRightOperand()
                            )
                    );
                    objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(
                            ((BinaryExpression) objExpressionToExpand).getLeftOperand()
                    );
                }


                if (null != objExpressionToExpand) {
                    objPartsCollection.addFirst(objExpressionToExpand);
                }

                return objPartsCollection;
            }
        };
    }
}
