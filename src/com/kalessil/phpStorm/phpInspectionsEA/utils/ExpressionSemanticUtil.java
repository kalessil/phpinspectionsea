package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class ExpressionSemanticUtil {
    /**
     * @param ifStatement if expression to check
     * @return boolean
     */
    public static boolean hasAlternativeBranches(If ifStatement) {
        return (null != ifStatement.getElseBranch() || ifStatement.getElseIfBranches().length > 0);
    }

    @Nullable
    public static PhpExpression getReturnValue(@NotNull PhpReturn objReturn) {
        for (PsiElement objChild : objReturn.getChildren()) {
            if (objChild instanceof PhpExpression) {
                return (PhpExpression) objChild;
            }
        }

        return null;
    }

    /**
     * @param objGroupStatement group expression to check
     * @return integer
     */
    public static int countExpressionsInGroup(GroupStatement objGroupStatement) {
        int intCountStatements = 0;
        for (PsiElement objStatement : objGroupStatement.getChildren()) {
            if (!(objStatement instanceof PhpPsiElement)) {
                continue;
            }

            ++intCountStatements;
        }

        return intCountStatements;
    }

    @Nullable
    public static PsiElement getLastStatement(@NotNull GroupStatement objGroupStatement) {
        PsiElement objLastChild = objGroupStatement.getLastChild();
        while (null != objLastChild) {
            if (objLastChild instanceof PhpPsiElement) {
                return objLastChild;
            }

            objLastChild = objLastChild.getPrevSibling();
        }

        return null;
    }

    /**
     * @param objControlExpression expression to scan for group definition
     * @return null|GroupStatement
     */
    @Nullable
    public static GroupStatement getGroupStatement(PsiElement objControlExpression) {
        for (PsiElement objChild : objControlExpression.getChildren()) {
            if (!(objChild instanceof GroupStatement)) {
                continue;
            }

            return (GroupStatement) objChild;
        }

        return null;
    }

    /**
     * @param objConstant expression to check
     * @return boolean
     */
    public static boolean isBoolean(ConstantReference objConstant){
        return (PhpLangUtil.isTrue(objConstant) || PhpLangUtil.isFalse(objConstant));
    }

    /**
     * @param objExpression to process
     * @return inner expression
     */
    @Nullable
    public static PsiElement getExpressionTroughParenthesis(@Nullable PsiElement objExpression) {
        if (!(objExpression instanceof ParenthesizedExpression)) {
            return objExpression;
        }

        PsiElement objInnerExpression = ((ParenthesizedExpression) objExpression).getArgument();
        while (objInnerExpression instanceof ParenthesizedExpression) {
            objInnerExpression = ((ParenthesizedExpression) objExpression).getArgument();
        }

        return objInnerExpression;
    }

    /**
     * @param objCondition to process
     * @return list of extracted conditions
     */
    @Nullable
    public static LinkedList<PsiElement> getConditions(@Nullable PsiElement objCondition, @Nullable IElementType[] arrOperationHolder) {
        /** get through unary and parenthesis wrappers */
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

        /** init container */
        LinkedList<PsiElement> objPartsCollection = new LinkedList<PsiElement>();

        /** return non-binary expressions, eg. callable execution */
        if (!(objCondition instanceof BinaryExpression)) {
            objPartsCollection.add(objCondition);
            return objPartsCollection;
        }


        /** check operation type and extract conditions */
        PsiElement objOperation = ((BinaryExpression) objCondition).getOperation();
        if (null == objOperation) {
            return null;
        }
        IElementType operationType = objOperation.getNode().getElementType();
        if (operationType != PhpTokenTypes.opOR && operationType != PhpTokenTypes.opAND) {
            return null;
        }

        if (null != arrOperationHolder) {
            arrOperationHolder[0] = operationType;
        }

        return ExpressionSemanticUtil.getConditions((BinaryExpression) objCondition, operationType);
    }

    /**
     * Extracts conditions into naturally ordered list
     *
     * @param objTarget expression for extracting sub-conditions
     * @param operationType operator to take in consideration
     * @return list of sub-conditions in native order
     */
    private static LinkedList<PsiElement> getConditions(BinaryExpression objTarget, IElementType operationType) {
        LinkedList<PsiElement> objPartsCollection = new LinkedList<PsiElement>();
        PsiElement objItemToAdd;

        /** right expression first */
        objItemToAdd = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getRightOperand());
        if (null != objItemToAdd) {
            objPartsCollection.add(objItemToAdd);
        }
        PsiElement objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getLeftOperand());

        /** expand binary operation while it's a binary operation */
        //noinspection ConstantConditions
        while (
                objExpressionToExpand instanceof BinaryExpression &&
                null != ((BinaryExpression) objExpressionToExpand).getOperation() &&
                null != ((BinaryExpression) objExpressionToExpand).getOperation().getNode() &&
                ((BinaryExpression) objExpressionToExpand).getOperation().getNode().getElementType() == operationType
        ) {
            objItemToAdd = ExpressionSemanticUtil.getExpressionTroughParenthesis(((BinaryExpression) objExpressionToExpand).getRightOperand());
            if (null != objItemToAdd) {
                objPartsCollection.addFirst(objItemToAdd);
            }
            objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(((BinaryExpression) objExpressionToExpand).getLeftOperand());
        }


        /** don't forget very first one */
        if (null != objExpressionToExpand) {
            objPartsCollection.addFirst(objExpressionToExpand);
        }

        return objPartsCollection;
    }

    @Nullable
    public static Function getScope(@NotNull PsiElement objExpression) {
        PsiElement objParent = objExpression.getParent();
        while (null != objParent && !(objParent instanceof PhpFile)) {
            if (objParent instanceof Function) {
                return (Function) objParent;
            }

            objParent = objParent.getParent();
        }

        return null;
    }

    @Nullable
    public static PsiElement getBlockScope(@NotNull PsiElement objExpression) {
        PsiElement objParent = objExpression.getParent();
        while (null != objParent && !(objParent instanceof PhpFile)) {
            if (
                objParent instanceof Function ||
                objParent instanceof PhpDocComment ||
                objParent instanceof PhpClass
            ) {
                return objParent;
            }

            objParent = objParent.getParent();
        }

        return null;
    }

    @Nullable
    public static LinkedList<Variable> getUseListVariables(@NotNull Function objFunction) {
        for (PsiElement objChild : objFunction.getChildren()) {
            /** iterated child is use list */
            if (objChild instanceof PhpUseList) {
                LinkedList<Variable> list = new LinkedList<Variable>();
                for (PsiElement objUseChild : objChild.getChildren()) {
                    /** collect variables */
                    if (objUseChild instanceof Variable) {
                        list.add((Variable) objUseChild);
                    }
                }

                return list;
            }
        }

        return null;
    }

    public static boolean isUsedAsLogicalOperand(@NotNull PsiElement expression) {
        PsiElement parent = expression.getParent();

        /* NON-implicit logical operand */
        if (parent instanceof If) {
            return true;
        }

        /* implicit logical operand */
        if (parent instanceof UnaryExpression) {
            PsiElement objOperation = ((UnaryExpression) parent).getOperation();
            if (null != objOperation && null != objOperation.getNode()){
                IElementType operationType = objOperation.getNode().getElementType();
                if (PhpTokenTypes.opNOT == operationType) {
                    return true;
                }
            }
        }

        /* NON-implicit logical operation in a group */
        if (parent instanceof BinaryExpression) {
            PsiElement objOperation = ((BinaryExpression) parent).getOperation();
            if (null != objOperation && null != objOperation.getNode()) {
                IElementType operationType = objOperation.getNode().getElementType();
                if (PhpTokenTypes.opAND == operationType || PhpTokenTypes.opOR == operationType) {
                    return true;
                }
            }
        }

        return false;
    }

    /** TODO: get BO type */
}
