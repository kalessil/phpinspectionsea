package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
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
    private static final String strProblemDescriptionBooleans  = "This boolean in condition makes no sense or enforces " +
            "condition result";

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

                LinkedList<PsiElement> objConditionsFromStatement = this.inspectExpressionsOrder(ifStatement.getCondition());
                if (null != objConditionsFromStatement) {
                    objAllConditions.addAll(objConditionsFromStatement);

                    this.inspectConditionsWithBooleans(objConditionsFromStatement);
                    objConditionsFromStatement.clear();
                }

                for (ElseIf objElseIf : ifStatement.getElseIfBranches()) {
                    objConditionsFromStatement = this.inspectExpressionsOrder(objElseIf.getCondition());
                    if (null != objConditionsFromStatement) {
                        objAllConditions.addAll(objConditionsFromStatement);

                        this.inspectConditionsWithBooleans(objConditionsFromStatement);
                        objConditionsFromStatement.clear();
                    }
                }

                this.inspectDuplicatedConditions(objAllConditions, ifStatement);
                /** TODO: If not binary/ternary/assignment/array access expression,  */
                /** TODO: perform types lookup - nullable core types/classes shall be compared with null.  */
                /** TODO: Inversion should be un-boxed to get expression. */
            }

            /***
             * Checks if any of conditions is boolean
             * @param objBranchConditions to check
             */
            private void inspectConditionsWithBooleans(LinkedList<PsiElement> objBranchConditions) {
                for (PsiElement objExpression : objBranchConditions) {
                    if (!(objExpression instanceof ConstantReference)) {
                        continue;
                    }

                    if (ExpressionSemanticUtil.isBoolean((ConstantReference) objExpression)) {
                        holder.registerProblem(objExpression, strProblemDescriptionBooleans, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }

            /**
             * Checks if duplicates are introduced, conditions collection will be modified so it's empty in the end
             * @param objAllConditions to check
             * @param ifStatement current scope
             */
            private void inspectDuplicatedConditions(LinkedList<PsiElement> objAllConditions, If ifStatement) {
                LinkedList<PsiElement> objParentConditions = new LinkedList<>();

                /** collect parent scopes conditions */
                PsiElement objParent = ifStatement.getParent();
                while (null != objParent && !(objParent instanceof PhpFile)) {
                    if (objParent instanceof If) {
                        LinkedList<PsiElement> tempList = ExpressionSemanticUtil.getConditions(((If) objParent).getCondition());
                        if (null != tempList) {
                            objParentConditions.addAll(tempList);
                            tempList.clear();
                        }

                        for (ElseIf objParentElseIf : ((If) objParent).getElseIfBranches()) {
                            tempList = ExpressionSemanticUtil.getConditions(objParentElseIf.getCondition());
                            if (null != tempList) {
                                objParentConditions.addAll(tempList);
                                tempList.clear();
                            }
                        }
                    }

                    objParent = objParent.getParent();
                }


                /** scan for duplicates */
                for (PsiElement objExpression : objAllConditions) {
                    if (null == objExpression) {
                        continue;
                    }

                    /** put a stub */
                    int intOuterIndex = objAllConditions.indexOf(objExpression);
                    objAllConditions.set(intOuterIndex, null);

                    /** ignore variables */
                    if (
                        objExpression instanceof Variable ||
                        objExpression instanceof ConstantReference ||
                        objExpression instanceof FieldReference
                    ) {
                        continue;
                    }

                    /** search duplicates in current scope */
                    for (PsiElement objInnerLoopExpression : objAllConditions) {
                        if (null == objInnerLoopExpression) {
                            continue;
                        }

                        boolean isDuplicate = PsiEquivalenceUtil.areElementsEquivalent(objInnerLoopExpression, objExpression);
                        if (isDuplicate) {
                            holder.registerProblem(objInnerLoopExpression, strProblemDescriptionDuplicate, ProblemHighlightType.WEAK_WARNING);

                            int intInnerIndex = objAllConditions.indexOf(objInnerLoopExpression);
                            objAllConditions.set(intInnerIndex, null);
                        }
                    }

                    /** search duplicates in outer scopes */
                    for (PsiElement objOuterScopeExpression : objParentConditions) {
                        if (null == objOuterScopeExpression) {
                            continue;
                        }

                        boolean isDuplicate = PsiEquivalenceUtil.areElementsEquivalent(objOuterScopeExpression, objExpression);
                        if (isDuplicate) {
                            holder.registerProblem(objExpression, strProblemDescriptionDuplicate, ProblemHighlightType.WEAK_WARNING);

                            int intOuterScopeIndex = objParentConditions.indexOf(objOuterScopeExpression);
                            objParentConditions.set(intOuterScopeIndex, null);
                        }
                    }
                }

                objParentConditions.clear();
            }

            /**
             * @param objCondition to inspect
             */
            @Nullable
            private LinkedList<PsiElement> inspectExpressionsOrder(PsiElement objCondition) {
                LinkedList<PsiElement> objPartsCollection = ExpressionSemanticUtil.getConditions(objCondition);
                if (null == objPartsCollection) {
                    return null;
                }

                /** one item only, skip costs estimation */
                if (objPartsCollection.size() < 2) {
                    return objPartsCollection;
                }

                /** verify if costs estimated are optimal */
                int intPreviousCost = 0;
                PsiElement objPreviousCond = null;

                int intLoopCurrentCost;
                boolean isPreviousCondArrayKeyExists;
                for (PsiElement objCond : objPartsCollection) {
                    intLoopCurrentCost = this.getExpressionCost(objCond);

                    /** special case when costs estimation is overridden with general practices */
                    isPreviousCondArrayKeyExists = (
                        null != objPreviousCond &&
                        objPreviousCond instanceof FunctionReference &&
                        null != ((FunctionReference) objPreviousCond).getName() &&
                        ((FunctionReference) objPreviousCond).getName().equals("array_key_exists")
                    );

                    if (intLoopCurrentCost < intPreviousCost && !isPreviousCondArrayKeyExists) {
                        holder.registerProblem(objCond, strProblemDescriptionOrdering, ProblemHighlightType.WEAK_WARNING);
                    }

                    intPreviousCost = intLoopCurrentCost;
                    objPreviousCond = objCond;
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
        };
    }
}
