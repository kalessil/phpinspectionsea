package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final public class ExpressionSemanticUtil {
    /**
     * @param ifStatement if expression to check
     * @return boolean
     */
    public static boolean hasAlternativeBranches(If ifStatement) {
        return (null != ifStatement.getElseBranch() || ifStatement.getElseIfBranches().length > 0);
    }

    /**
     * TODO: to re-check if API already has a method for this
     */
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
     * @param groupStatement group expression to check
     * @return integer
     */
    public static int countExpressionsInGroup(@NotNull GroupStatement groupStatement) {
        int count = 0;
        for (PsiElement statement : groupStatement.getChildren()) {
            if (!(statement instanceof PhpPsiElement) || statement instanceof PhpDocType) {
                continue;
            }
            /* skip doc-blocks */
            if (statement instanceof PhpDocComment){
                continue;
            }

            ++count;
        }

        return count;
    }

    @Nullable
    public static PsiElement getLastStatement(@NotNull GroupStatement groupStatement) {
        PsiElement lastChild = groupStatement.getLastChild();
        while (null != lastChild) {
            if (lastChild instanceof PhpPsiElement && !(lastChild instanceof PhpDocComment)) {
                return lastChild;
            }

            lastChild = lastChild.getPrevSibling();
        }

        return null;
    }

    /**
     * @param expression expression to scan for group definition
     * @return null|GroupStatement
     */
    @Nullable
    public static GroupStatement getGroupStatement(@NotNull PsiElement expression) {
        for (PsiElement child : expression.getChildren()) {
            if (!(child instanceof GroupStatement)) {
                continue;
            }

            return (GroupStatement) child;
        }

        return null;
    }

    /**
     * @param expression to process
     * @return inner expression
     */
    @Nullable
    public static PsiElement getExpressionTroughParenthesis(@Nullable PsiElement expression) {
        if (!(expression instanceof ParenthesizedExpression)) {
            return expression;
        }

        PsiElement innerExpression = ((ParenthesizedExpression) expression).getArgument();
        while (innerExpression instanceof ParenthesizedExpression) {
            innerExpression = ((ParenthesizedExpression) innerExpression).getArgument();
        }

        return innerExpression;
    }

    /**
     * @param objCondition to process
     * @return list of extracted conditions
     */
    @Nullable
    public static List<PsiElement> getConditions(@Nullable PsiElement objCondition, @Nullable IElementType[] arrOperationHolder) {
        /* get through unary and parenthesis wrappers */
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

        /* init container */
        List<PsiElement> objPartsCollection = new ArrayList<>();

        /* return non-binary expressions, eg. callable execution */
        if (!(objCondition instanceof BinaryExpression)) {
            objPartsCollection.add(objCondition);
            return objPartsCollection;
        }


        /* check operation type and extract conditions */
        PsiElement objOperation = ((BinaryExpression) objCondition).getOperation();
        if (null == objOperation) {
            return null;
        }
        IElementType operationType = objOperation.getNode().getElementType();
        if (operationType != PhpTokenTypes.opOR && operationType != PhpTokenTypes.opAND) {
            /* binary expression, but not needed type => return it */
            objPartsCollection.add(objCondition);
            return objPartsCollection;
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
    private static List<PsiElement> getConditions(BinaryExpression objTarget, IElementType operationType) {
        LinkedList<PsiElement> objPartsCollection = new LinkedList<>();
        PsiElement objItemToAdd;

        /* right expression first */
        objItemToAdd = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getRightOperand());
        if (null != objItemToAdd) {
            objPartsCollection.add(objItemToAdd);
        }
        PsiElement objExpressionToExpand = ExpressionSemanticUtil.getExpressionTroughParenthesis(objTarget.getLeftOperand());

        /* expand binary operation while it's a binary operation */
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


        /* don't forget very first one */
        if (null != objExpressionToExpand) {
            objPartsCollection.addFirst(objExpressionToExpand);
        }

        return new ArrayList<>(objPartsCollection);
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
    public static PsiElement getBlockScope(@NotNull PsiElement expression) {
        PsiElement parent = expression.getParent();
        while (null != parent && !(parent instanceof PhpFile)) {
            if (
                parent instanceof Function ||
                parent instanceof PhpDocComment ||
                parent instanceof PhpClass
            ) {
                return parent;
            }

            parent = parent.getParent();
        }

        return null;
    }

    @Nullable
    public static List<Variable> getUseListVariables(@NotNull Function function) {
        for (PsiElement child : function.getChildren()) {
            /* iterated child is use list */
            if (child instanceof PhpUseList) {
                final List<Variable> list = new ArrayList<>();
                for (PsiElement variableCandidate : child.getChildren()) {
                    if (variableCandidate instanceof Variable) {
                        list.add((Variable) variableCandidate);
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
        if (parent instanceof TernaryExpression && expression == ((TernaryExpression) parent).getCondition()) {
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
                if (
                    PhpTokenTypes.opAND == operationType     || PhpTokenTypes.opOR == operationType ||
                    PhpTokenTypes.opLIT_AND == operationType || PhpTokenTypes.opLIT_OR == operationType
                ) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    public static StringLiteralExpression resolveAsStringLiteral(@Nullable PsiElement expression) {
        if (null == expression) {
            return null;
        }
        expression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression);

        if (expression instanceof StringLiteralExpression) {
            return (StringLiteralExpression) expression;
        }

        if (expression instanceof FieldReference || expression instanceof ClassConstantReference) {
            final Field fieldOrConstant = (Field) ((MemberReference) expression).resolve();
            if (null != fieldOrConstant && fieldOrConstant.getDefaultValue() instanceof StringLiteralExpression) {
                return (StringLiteralExpression) fieldOrConstant.getDefaultValue();
            }
        }

        if (expression instanceof Variable) {
            final String variable = ((Variable) expression).getName();
            if (!StringUtil.isEmpty(variable)) {
                final Function scope = ExpressionSemanticUtil.getScope(expression);
                if (null != scope) {
                    final Set<AssignmentExpression> matched = new HashSet<>();

                    Collection<AssignmentExpression> assignments
                            = PsiTreeUtil.findChildrenOfType(scope, AssignmentExpression.class);
                    /* collect self-assignments as well */
                    for (AssignmentExpression assignment : assignments) {
                        if (assignment.getVariable() instanceof Variable && assignment.getValue() instanceof StringLiteralExpression) {
                            final String name = assignment.getVariable().getName();
                            if (!StringUtil.isEmpty(name) && name.equals(variable)) {
                                matched.add(assignment);
                            }
                        }
                    }
                    assignments.clear();

                    if (matched.size() == 1) {
                        StringLiteralExpression result = (StringLiteralExpression) matched.iterator().next().getValue();

                        matched.clear();
                        return result;
                    }
                    matched.clear();
                }
            }
        }

        return null;
    }

    /* TODO: get BO type */
}
