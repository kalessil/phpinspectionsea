package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
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
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class NotOptimalIfConditionsInspection extends BasePhpInspection {
    private static final String strProblemDescriptionOrdering  = "This condition execution costs less than previous one";
    private static final String strProblemDescriptionDuplicate = "This condition duplicated in other if/elseif branch";

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
                LinkedList<PsiElement> objAllConditions = new LinkedList<>();

                LinkedList<PsiElement> objConditionsFromStatement = this.inspectExpression(ifStatement.getCondition());
                if (null != objConditionsFromStatement) {
                    objAllConditions.addAll(objConditionsFromStatement);
                    objConditionsFromStatement.clear();
                }

                for (ElseIf objElseIf : ifStatement.getElseIfBranches()) {
                    objConditionsFromStatement = this.inspectExpression(objElseIf.getCondition());
                    if (null != objConditionsFromStatement) {
                        objAllConditions.addAll(objConditionsFromStatement);
                        objConditionsFromStatement.clear();
                    }
                }

                this.inspectDuplicatedConditions(objAllConditions);
            }

            /**
             * Checks if duplicates are introduced, conditions collection will be modified so it's empty in the end
             * @param objAllConditions to check
             */
            private void inspectDuplicatedConditions(LinkedList<PsiElement> objAllConditions) {
                for (PsiElement objExpression : objAllConditions) {
                    if (null == objExpression) {
                        continue;
                    }

                    /** put a stub */
                    int intOuterIndex = objAllConditions.indexOf(objExpression);
                    objAllConditions.set(intOuterIndex, null);

                    /** ignore variables */
                    if (objExpression instanceof Variable) {
                        continue;
                    }

                    /** search duplicates */
                    for (PsiElement objInnerLoopExpression : objAllConditions) {
                        if (null == objInnerLoopExpression) {
                            continue;
                        }

                        /**
                         * not really helpful, needs to be extended by taking in account binary operations
                         * with equal arguments
                         */
                        boolean isDuplicate = PsiEquivalenceUtil.areElementsEquivalent(objInnerLoopExpression, objExpression);
                        if (isDuplicate) {
                            holder.registerProblem(objInnerLoopExpression, strProblemDescriptionDuplicate, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            int intInnerIndex = objAllConditions.indexOf(objInnerLoopExpression);
                            objAllConditions.set(intInnerIndex, null);
                        }
                    }
                }
            }

            /**
             * @param objCondition to inspect
             */
            @Nullable
            private LinkedList<PsiElement> inspectExpression (PsiElement objCondition) {
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
                    return null;
                }


                /** analyse expression */
                if (!(objCondition instanceof BinaryExpression)) {
                    return null;
                }

                return this.analyseBinaryExpression((BinaryExpression) objCondition);
            }

            /**
             * Hi-level analysis on top of conditions and costs
             *
             * @param objTarget to analyse
             */
            @Nullable
            private LinkedList<PsiElement> analyseBinaryExpression(BinaryExpression objTarget) {
                /** meet pre-conditions */
                PsiElement objOperation = objTarget.getOperation();
                if (null == objOperation) {
                    return null;
                }
                String strOperation = objOperation.getText();
                if (
                    !strOperation.equals("&&") &&
                    !strOperation.equals("||")
                ) {
                    return null;
                }

                /** extract conditions in natural order */
                LinkedList<PsiElement> objPartsCollection = this.extractConditionParts(objTarget, strOperation);

                /** verify if costs estimated are optimal */
                int intLoopCurrentCost;
                int intPreviousCost = 0;
                for (PsiElement objCond : objPartsCollection) {
                    intLoopCurrentCost = this.getExpressionCost(objCond);

                    if (intLoopCurrentCost < intPreviousCost) {
                        holder.registerProblem(objCond, strProblemDescriptionOrdering, ProblemHighlightType.WEAK_WARNING);
                    }

                    intPreviousCost = intLoopCurrentCost;
                }
                return objPartsCollection;
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
                    objExpression instanceof Variable ||
                    null == objExpression
                ) {
                    return 0;
                }

                if (
                    objExpression instanceof ClassConstantReference ||
                    objExpression instanceof FieldReference
                ) {
                    return 1;
                }

                if (
                    objExpression instanceof ArrayAccessExpression ||
                    objExpression instanceof PhpEmpty ||
                    objExpression instanceof PhpIsset ||
                    objExpression instanceof PhpUnset
                ) {
                    return 2;
                }

                if (
                    objExpression instanceof MethodReference ||
                    objExpression instanceof FunctionReference
                ) {
                    int intArgumentsCost = 0;
                    for (PsiElement objParameter : ((FunctionReference) objExpression).getParameters()) {
                        intArgumentsCost += this.getExpressionCost(objParameter);
                    }

                    return (5 + intArgumentsCost);
                }

                if (objExpression instanceof UnaryExpression) {
                    return this.getExpressionCost(((UnaryExpression) objExpression).getValue());
                }

/*                if (objExpression instanceof TernaryExpression) {
                    return
                        this.getExpressionCost(((TernaryExpression) objExpression).getCondition()) +
                        Math.max(
                                this.getExpressionCost(((TernaryExpression) objExpression).getTrueVariant()),
                                this.getExpressionCost(((TernaryExpression) objExpression).getFalseVariant())
                        );
                }
*/

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
                PsiElement objItemToAdd;

                objItemToAdd = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getRightOperand());
                if (null != objItemToAdd) {
                    objPartsCollection.add(objItemToAdd);
                }
                PsiElement objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getLeftOperand());

                //noinspection ConstantConditions
                while (
                    objExpressionToExpand instanceof BinaryExpression &&
                    ((BinaryExpression) objExpressionToExpand).getOperation() != null &&
                    ((BinaryExpression) objExpressionToExpand).getOperation().getText().equals(strOperation)
                ) {
                    objItemToAdd = ExpressionSemanticUtil.getExpressionTroughParenthesis(((BinaryExpression) objExpressionToExpand).getRightOperand());
                    if (null != objItemToAdd) {
                        objPartsCollection.addFirst(objItemToAdd);
                    }
                    objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(((BinaryExpression) objExpressionToExpand).getLeftOperand());
                }


                if (null != objExpressionToExpand) {
                    objPartsCollection.addFirst(objExpressionToExpand);
                }

                return objPartsCollection;
            }
        };
    }
}
