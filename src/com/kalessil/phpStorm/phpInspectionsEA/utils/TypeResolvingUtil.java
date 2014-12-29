package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.LinkedList;

public class TypeResolvingUtil {
    /** adds type, handling | and #, invoking signatures resolving */
    private static void storeType(String strType, LinkedList<String> objTypesCollection) {
        if (strType.contains("|")) {
            for (String strOneType : strType.split("\\|")) {
                storeType(Types.getType(strOneType), objTypesCollection);
            }
            return;
        }

        if (StringUtil.isEmpty(strType) || strType.contains("#") || strType.contains("?")) {
            return;
        }

        objTypesCollection.add(Types.getType(strType));
    }

    /** high-level resolving logic */
    public static void resolveExpressionType(PsiElement objExpression, LinkedList<String> objTypesCollection) {
        objExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objExpression);

        if (objExpression instanceof ArrayCreationExpression) {
            objTypesCollection.add(Types.strArray);
            return;
        }
        if (objExpression instanceof StringLiteralExpression) {
            objTypesCollection.add(Types.strString);
            return;
        }

        if (objExpression instanceof ConstantReference) {
            TypeResolvingUtil.resolveConstantReference((ConstantReference) objExpression, objTypesCollection);
            return;
        }
        if (objExpression instanceof NewExpression) {
            TypeResolvingUtil.resolveNewExpression((NewExpression) objExpression, objTypesCollection);
            return;
        }

        if (objExpression instanceof TernaryExpression) {
            TypeResolvingUtil.resolveTernaryOperator((TernaryExpression) objExpression, objTypesCollection);
            return;
        }
        if (objExpression instanceof UnaryExpression) {
            TypeResolvingUtil.resolveUnaryExpression((UnaryExpression) objExpression, objTypesCollection);
            return;
        }
        if (objExpression instanceof BinaryExpression) {
            TypeResolvingUtil.resolveBinaryExpression((BinaryExpression) objExpression, objTypesCollection);
            return;
        }
        if (objExpression instanceof SelfAssignmentExpression) {
            TypeResolvingUtil.resolveSelfAssignmentExpression((SelfAssignmentExpression) objExpression, objTypesCollection);
            return;
        }

        /** TODO: implement - start */
        if (objExpression instanceof Variable) {
            return;
        }
        if (
            objExpression instanceof MethodReference ||
            objExpression instanceof FieldReference ||
            objExpression instanceof ClassConstantReference
        ) {
            return;
        }
        if (objExpression instanceof FunctionReference) {
            return;
        }
        if (objExpression instanceof ArrayAccessExpression) {
            return;
        }
        /** TODO: end */


        if (objExpression instanceof PhpExpression) {
            TypeResolvingUtil.resolvePhpExpression((PhpExpression) objExpression, objTypesCollection);
            return;
        }

        /** TODO: check which case is not worked out */
        //objTypesCollection.add(Types.strNotProcessed);
    }

    /** resolve numbers and exotic structures, eg list() = .... */
    private static void resolvePhpExpression(PhpExpression objExpression, LinkedList<String> objTypesCollection) {
        String strType = objExpression.getType().toString();
        if (StringUtil.isEmpty(strType)) {
            return;
        }

        storeType(strType, objTypesCollection);
    }

    /** Will resolve self-assignments */
    private static void resolveSelfAssignmentExpression(SelfAssignmentExpression objExpression, LinkedList<String> objTypesCollection) {
        String strType = objExpression.getType().toString();
        if (StringUtil.isEmpty(strType)) {
            return;
        }

        storeType(strType, objTypesCollection);
    }

    /** Will resolve type of new expression */
    private static void resolveNewExpression(NewExpression objExpression, LinkedList<String> objTypesCollection) {
        ClassReference objClassRef = objExpression.getClassReference();
        if (null == objClassRef || null == objClassRef.getFQN()) {
            objTypesCollection.add(Types.strResolvingAbortedOnPsiLevel);
            return;
        }

        objTypesCollection.add(objClassRef.getFQN());
    }

    /** resolve some of binary expressions . | && | || */
    private static void resolveBinaryExpression (BinaryExpression objExpression, LinkedList<String> objTypesCollection) {
        PsiElement objOperation = objExpression.getOperation();
        if (null == objOperation) {
            return;
        }

        IElementType objOperationType = objOperation.getNode().getElementType();
        if (objOperationType == PhpTokenTypes.opCONCAT) {
            objTypesCollection.add(Types.strString);
            return;
        } else if (objOperationType == PhpTokenTypes.opAND || objOperationType == PhpTokenTypes.opAND) {
            objTypesCollection.add(Types.strBoolean);
            return;
        }

        storeType(objExpression.getType().toString(), objTypesCollection);
    }

    /** Resolve type casting expressions */
    private static void resolveUnaryExpression (UnaryExpression objExpression, LinkedList<String> objTypesCollection) {
        PsiElement objOperation = objExpression.getOperation();
        if (null == objOperation) {
            return;
        }

        IElementType objType = objOperation.getNode().getElementType();
        if (PhpTokenTypes.CAST_OPERATORS.contains(objType)) {
            // TODO: add this opOBJECT_CAST, opUNSET_CAST, opBINARY_CAST
            if (objType == PhpTokenTypes.opINTEGER_CAST) {
                objTypesCollection.add(Types.strInteger);
            } else if (objType == PhpTokenTypes.opARRAY_CAST) {
                objTypesCollection.add(Types.strArray);
            } else if (objType == PhpTokenTypes.opBOOLEAN_CAST) {
                objTypesCollection.add(Types.strBoolean);
            } else if (objType == PhpTokenTypes.opFLOAT_CAST) {
                objTypesCollection.add(Types.strFloat);
            } else if (objType == PhpTokenTypes.opSTRING_CAST) {
                objTypesCollection.add(Types.strString);
            }

            return;
        }

        storeType(objExpression.getType().toString(), objTypesCollection);
    }

    /** Will resolve constants references */
    private static void resolveConstantReference (ConstantReference objExpression, LinkedList<String> objTypesCollection) {
        if (ExpressionSemanticUtil.isBoolean(objExpression)) {
            objTypesCollection.add(Types.strBoolean);
            return;
        }

        if (PhpLangUtil.isNull(objExpression)) {
            objTypesCollection.add(Types.strNull);
            return;
        }

        storeType(objExpression.getType().toString(), objTypesCollection);
    }

    /** Will resolve ternary operator */
    private static void resolveTernaryOperator (TernaryExpression objExpression, LinkedList<String> objTypesCollection) {
        if (null != objExpression.getTrueVariant()) {
            TypeResolvingUtil.resolveExpressionType(objExpression.getTrueVariant(), objTypesCollection);
        }

        if (null != objExpression.getFalseVariant()) {
            TypeResolvingUtil.resolveExpressionType(objExpression.getFalseVariant(), objTypesCollection);
        }
    }
}
