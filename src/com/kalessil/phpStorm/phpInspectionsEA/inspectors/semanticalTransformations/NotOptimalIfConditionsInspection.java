package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class NotOptimalIfConditionsInspection extends BasePhpInspection {
    private static final String strProblemDescription = "This condition execution costs less than previous one";

    @NotNull
    public String getDisplayName() {
        return "Semantics: not optimal if conditions";
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
                this.inspectExpression(ifStatement.getCondition());

                for (ElseIf objElseIf : ifStatement.getElseIfBranches()) {
                    this.inspectExpression(objElseIf.getCondition());
                }
            }

            /**
             * @param objCondition to inspect
             */
            private void inspectExpression (PsiElement objCondition) {
                /** full-fill pre-requirements */
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


                /** analyse expression */
                if (!(objCondition instanceof BinaryExpression)) {
                    return;
                }
                this.analyseBinaryExpression((BinaryExpression) objCondition);
            }

            /**
             * Hi-level analysis on top of conditions and costs
             *
             * @param objTarget to analyse
             */
            private void analyseBinaryExpression(BinaryExpression objTarget) {
                /** meet pre-conditions */
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

                /** extract conditions in natural order */
                LinkedList<PsiElement> objPartsCollection = this.extractConditionParts(objTarget, strOperation);

                /** verify if costs estimated are optimal */
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
             * Estimates execution cost on basis 0-10 for simple parts. Complex constructions can be estimated
             * to more than 10.
             *
             * @param objExpression to estimate for execution cost
             * @return costs
             */
            private int getExpressionCost(PsiElement objExpression) {
                objExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objExpression);

                if (
                    objExpression instanceof ConstantReference ||
                    objExpression instanceof StringLiteralExpression ||
                    objExpression instanceof ClassReference ||
                    null == objExpression
                ) {
                    return 0;
                }

                if (
                    objExpression instanceof ClassConstantReference ||
                    objExpression instanceof FieldReference ||
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
                    /** TODO: weight cases: isset(weight = 0) and parameters weight */
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


                if (
                    objExpression instanceof PhpExpression &&
                    objExpression.getNode().getElementType() == PhpElementTypes.NUMBER
                ) {
                    return 0;
                }

                return 10;
            }

            /**
             * Extracts conditions into naturally ordered list
             *
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
