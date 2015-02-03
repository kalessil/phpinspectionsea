package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;

public class NotOptimalIfConditionsInspection extends BasePhpInspection {
    private static final String strProblemDescriptionInstanceOfComplementarity = "Probable bug: ensure this behave properly with instanceof in this conditions set";
    private static final String strProblemDescriptionOrdering  = "This condition execution costs less than previous one";
    private static final String strProblemDescriptionDuplicateConditions = "This condition duplicated in other if/elseif branch";
    private static final String strProblemDescriptionBooleans  = "This boolean in condition makes no sense or enforces condition result";
    private static final String strProblemDescriptionDuplicateConditionPart = "This call is duplicated in conditions set";
    private static final String strProblemDescriptionIssetCanBeMergedAndCase = "This can be merged into previous 'isset(..., ...[, ...])'";
    private static final String strProblemDescriptionIssetCanBeMergedOrCase = "This can be merged into previous '!isset(..., ...[, ...])'";
    private static final String strProblemDescriptionConditionShallBeWrapped = "Confusing conditions structure: please wrap with '(...)'";

    private HashSet<String> functionsSet = null;
    private HashSet<String> getFunctionsSet() {
        if (null == functionsSet) {
            functionsSet = new HashSet<String>();

            functionsSet.add("array_key_exists");
            functionsSet.add("is_array");
            functionsSet.add("is_string");
            functionsSet.add("is_bool");
            functionsSet.add("is_int");
            functionsSet.add("is_float");
            functionsSet.add("is_resource");
        }

        return functionsSet;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                LinkedList<PsiElement> objAllConditions = new LinkedList<PsiElement>();
                IElementType[] arrOperationHolder = { null };

                LinkedList<PsiElement> objConditionsFromStatement = this.inspectExpressionsOrder(ifStatement.getCondition(), arrOperationHolder);
                if (null != objConditionsFromStatement) {
                    objAllConditions.addAll(objConditionsFromStatement);

                    this.inspectConditionsWithBooleans(objConditionsFromStatement);
                    this.inspectConditionsForMissingParenthesis(objConditionsFromStatement);
                    this.inspectConditionsForDuplicatedCalls(objConditionsFromStatement);
                    this.inspectConditionsForMultipleIsSet(objConditionsFromStatement, arrOperationHolder[0]);
                    this.inspectConditionsForInstanceOfAndIdentityOperations(objConditionsFromStatement, arrOperationHolder[0]);

                    objConditionsFromStatement.clear();
                }

                for (ElseIf objElseIf : ifStatement.getElseIfBranches()) {
                    objConditionsFromStatement = this.inspectExpressionsOrder(objElseIf.getCondition(), arrOperationHolder);
                    if (null != objConditionsFromStatement) {
                        objAllConditions.addAll(objConditionsFromStatement);

                        this.inspectConditionsWithBooleans(objConditionsFromStatement);
                        this.inspectConditionsForMissingParenthesis(objConditionsFromStatement);
                        this.inspectConditionsForDuplicatedCalls(objConditionsFromStatement);
                        this.inspectConditionsForMultipleIsSet(objConditionsFromStatement, arrOperationHolder[0]);
                        this.inspectConditionsForInstanceOfAndIdentityOperations(objConditionsFromStatement, arrOperationHolder[0]);

                        objConditionsFromStatement.clear();
                    }
                }

                this.inspectDuplicatedConditions(objAllConditions, ifStatement);
                /** TODO: If not binary/ternary/assignment/array access expression,  */
                /** TODO: perform types lookup - nullable core types/classes shall be compared with null.  */
                /** TODO: Inversion should be un-boxed to get expression. */
            }

            private void inspectConditionsForMissingParenthesis(@NotNull LinkedList<PsiElement> objBranchConditions) {
                for (PsiElement objCondition : objBranchConditions) {
                    if (!(objCondition instanceof BinaryExpression)) {
                        continue;
                    }

                    PsiElement objOperation = ((BinaryExpression) objCondition).getOperation();
                    if (null == objOperation) {
                        continue;
                    }
                    IElementType operationType = objOperation.getNode().getElementType();
                    if (operationType != PhpTokenTypes.opAND && operationType != PhpTokenTypes.opOR) {
                        continue;
                    }

                    if (!(objCondition.getParent() instanceof ParenthesizedExpression)) {
                        holder.registerProblem(objCondition, strProblemDescriptionConditionShallBeWrapped, ProblemHighlightType.ERROR);
                    }
                }
            }

            /** TODO: is_* functions */
            private void inspectConditionsForInstanceOfAndIdentityOperations(@NotNull LinkedList<PsiElement> objBranchConditions, @Nullable IElementType operationType) {
                if (operationType != PhpTokenTypes.opAND || objBranchConditions.size() < 2) {
                    return;
                }

                PsiElement objTestSubject = null;
                for (PsiElement objExpression : objBranchConditions) {
                    if (objExpression instanceof BinaryExpression) {
                        BinaryExpression objInstanceOfExpression = (BinaryExpression) objExpression;
                        if (
                            null != objInstanceOfExpression.getOperation() &&
                            objInstanceOfExpression.getOperation().getNode().getElementType() == PhpTokenTypes.kwINSTANCEOF
                        ) {
                            objTestSubject = objInstanceOfExpression.getLeftOperand();
                            break;
                        }
                    }
                }
                if (null == objTestSubject) {
                    return;
                }

                for (PsiElement objExpression : objBranchConditions) {
                    if (objExpression instanceof BinaryExpression) {
                        BinaryExpression objBinaryExpression = (BinaryExpression) objExpression;
                        if (
                            null != objBinaryExpression.getOperation() &&
                            null != objBinaryExpression.getLeftOperand() &&
                            null != objBinaryExpression.getRightOperand()

                        ) {
                            IElementType objConditionOperation = objBinaryExpression.getOperation().getNode().getElementType();
                            if (objConditionOperation == PhpTokenTypes.opIDENTICAL || objConditionOperation == PhpTokenTypes.opNOT_IDENTICAL) {
                                if (
                                    PsiEquivalenceUtil.areElementsEquivalent(objTestSubject, objBinaryExpression.getLeftOperand()) ||
                                    PsiEquivalenceUtil.areElementsEquivalent(objTestSubject, objBinaryExpression.getRightOperand())
                                ) {
                                    holder.registerProblem(objExpression, strProblemDescriptionInstanceOfComplementarity, ProblemHighlightType.WEAK_WARNING);
                                }
                            }
                        }
                    }
                }
            }

            private void inspectConditionsForMultipleIsSet(@NotNull LinkedList<PsiElement> objBranchConditions, @Nullable IElementType operationType) {
                /** handle isset && isset ... */
                if (operationType == PhpTokenTypes.opAND) {
                    int intIssetCallsCount = 0;
                    for (PsiElement objExpression : objBranchConditions) {
                        if (!(objExpression instanceof PhpIsset)) {
                            continue;
                        }

                        ++intIssetCallsCount;
                        if (intIssetCallsCount > 1) {
                            holder.registerProblem(objExpression, strProblemDescriptionIssetCanBeMergedAndCase, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }

                    return;
                }

                /** handle !isset || !isset ... */
                if (operationType == PhpTokenTypes.opOR) {
                    int intIssetCallsCount = 0;
                    for (PsiElement objExpression : objBranchConditions) {
                        if (!(objExpression instanceof UnaryExpression)) {
                            continue;
                        }
                        objExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(((UnaryExpression) objExpression).getValue());
                        if (!(objExpression instanceof PhpIsset)) {
                            continue;
                        }

                        ++intIssetCallsCount;
                        if (intIssetCallsCount > 1) {
                            holder.registerProblem(objExpression, strProblemDescriptionIssetCanBeMergedOrCase, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }

            private void inspectConditionsForDuplicatedCalls(@NotNull LinkedList<PsiElement> objBranchConditions) {
                if (objBranchConditions.size() < 2) {
                    return;
                }

                /** extract calls */
                LinkedList<PsiElement> objCallsExtracted = new LinkedList<PsiElement>();
                for (PsiElement objCondition : objBranchConditions) {
                    if (!(objCondition instanceof BinaryExpression)) {
                        continue;
                    }

                    PsiElement tempExpression = ((BinaryExpression) objCondition).getLeftOperand();
                    if (tempExpression instanceof MethodReference || tempExpression instanceof FunctionReference) {
                        objCallsExtracted.add(tempExpression);
                    }

                    tempExpression = ((BinaryExpression) objCondition).getRightOperand();
                    if (tempExpression instanceof MethodReference || tempExpression instanceof FunctionReference) {
                        objCallsExtracted.add(tempExpression);
                    }
                }

                /** scan for duplicates */
                for (PsiElement objExpression : objCallsExtracted) {
                    if (null == objExpression) {
                        continue;
                    }

                    /** put a stub */
                    int intOuterIndex = objCallsExtracted.indexOf(objExpression);
                    objCallsExtracted.set(intOuterIndex, null);

                    /** search duplicates in current scope */
                    for (PsiElement objInnerLoopExpression : objCallsExtracted) {
                        if (null == objInnerLoopExpression) {
                            continue;
                        }

                        boolean isDuplicate = PsiEquivalenceUtil.areElementsEquivalent(objInnerLoopExpression, objExpression);
                        if (isDuplicate) {
                            holder.registerProblem(objInnerLoopExpression, strProblemDescriptionDuplicateConditionPart, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            int intInnerIndex = objCallsExtracted.indexOf(objInnerLoopExpression);
                            objCallsExtracted.set(intInnerIndex, null);
                        }
                    }
                }

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
                LinkedList<PsiElement> objParentConditions = new LinkedList<PsiElement>();

                /** collect parent scopes conditions */
                PsiElement objParent = ifStatement.getParent();
                while (null != objParent && !(objParent instanceof PhpFile)) {
                    if (objParent instanceof If) {
                        LinkedList<PsiElement> tempList = ExpressionSemanticUtil.getConditions(((If) objParent).getCondition(), null);
                        if (null != tempList) {
                            objParentConditions.addAll(tempList);
                            tempList.clear();
                        }

                        for (ElseIf objParentElseIf : ((If) objParent).getElseIfBranches()) {
                            tempList = ExpressionSemanticUtil.getConditions(objParentElseIf.getCondition(), null);
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
                            holder.registerProblem(objInnerLoopExpression, strProblemDescriptionDuplicateConditions, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

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
                            holder.registerProblem(objExpression, strProblemDescriptionDuplicateConditions, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

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
            private LinkedList<PsiElement> inspectExpressionsOrder(PsiElement objCondition, @Nullable IElementType[] arrOperationHolder) {
                LinkedList<PsiElement> objPartsCollection = ExpressionSemanticUtil.getConditions(objCondition, arrOperationHolder);
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
                HashSet<String> functionsSetToAllow = getFunctionsSet();

                int intLoopCurrentCost;
                boolean isPreviousCondCostCanBeBigger;
                for (PsiElement objCond : objPartsCollection) {
                    intLoopCurrentCost = this.getExpressionCost(objCond);

                    /** special case when costs estimation is overridden with general practices */
                    isPreviousCondCostCanBeBigger = (
                        objPreviousCond instanceof FunctionReference &&
                        functionsSetToAllow.contains(((FunctionReference) objPreviousCond).getName())
                    );

                    if (!isPreviousCondCostCanBeBigger && intLoopCurrentCost < intPreviousCost) {
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

                    if (objExpression instanceof MethodReference) {
                        intArgumentsCost += this.getExpressionCost(((MethodReference) objExpression).getFirstPsiChild());
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

                if (objExpression instanceof ArrayCreationExpression) {
                    int intCosts = 0;
                    for (ArrayHashElement objEntry : ((ArrayCreationExpression) objExpression).getHashElements()) {
                        intCosts += this.getExpressionCost(objEntry.getKey());
                        intCosts += this.getExpressionCost(objEntry.getValue());
                    }
                    return intCosts;
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
