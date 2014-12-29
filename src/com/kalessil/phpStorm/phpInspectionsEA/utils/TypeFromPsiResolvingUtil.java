package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.LinkedList;

public class TypeFromPsiResolvingUtil {
    /** adds type, handling | and #, invoking signatures resolving */
    private static void storeAsTypeWithSignaturesImport(String strType, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
        if (strType.contains("|")) {
            for (String strOneType : strType.split("\\|")) {
                storeAsTypeWithSignaturesImport(Types.getType(strOneType), objIndex, objTypesCollection);
            }
            return;
        }

        if (StringUtil.isEmpty(strType) || strType.equals("?")) {
            return;
        }

        if (strType.contains("#")) {
            TypeFromSignatureResolvingUtil.resolveSignature(strType, objIndex, objTypesCollection);
            return;
        }

        objTypesCollection.add(Types.getType(strType));
    }

    /** high-level resolving logic */
    public static void resolveExpressionType(PsiElement objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
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
            resolveConstantReference((ConstantReference) objExpression, objIndex, objTypesCollection);
            return;
        }

        if (objExpression instanceof TernaryExpression) {
            resolveTernaryOperator((TernaryExpression) objExpression, objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof UnaryExpression) {
            resolveUnaryExpression((UnaryExpression) objExpression, objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof BinaryExpression) {
            resolveBinaryExpression((BinaryExpression) objExpression, objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof SelfAssignmentExpression) {
            resolveSelfAssignmentExpression((SelfAssignmentExpression) objExpression, objIndex, objTypesCollection);
            return;
        }


        if (objExpression instanceof Variable) {
            /** TODO: pre-defined variables */
            storeAsTypeWithSignaturesImport(((Variable) objExpression).getSignature(), objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof ArrayAccessExpression) {
            storeAsTypeWithSignaturesImport(((ArrayAccessExpression) objExpression).getType().toString(), objIndex, objTypesCollection);
            return;
        }


        if (objExpression instanceof NewExpression) {
            resolveNewExpression((NewExpression) objExpression, objTypesCollection);
            return;
        }
        if (objExpression instanceof ClassConstantReference) {
            storeAsTypeWithSignaturesImport(((ClassConstantReference) objExpression).getSignature(), objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof FieldReference) {
            storeAsTypeWithSignaturesImport(((FieldReference) objExpression).getSignature(), objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof MethodReference) {
            storeAsTypeWithSignaturesImport(((MethodReference) objExpression).getSignature(), objIndex, objTypesCollection);
            return;
        }
        if (objExpression instanceof FunctionReference) {
            storeAsTypeWithSignaturesImport(((FunctionReference) objExpression).getSignature(), objIndex, objTypesCollection);
            return;
        }


        if (objExpression instanceof PhpExpression) {
            resolvePhpExpression((PhpExpression) objExpression, objIndex, objTypesCollection);
            return;
        }

        /** TODO: check which case is not worked out */
        //objTypesCollection.add(Types.strNotProcessed);
    }

    /** resolve numbers and exotic structures, eg list() = .... */
    private static void resolvePhpExpression(PhpExpression objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
        String strType = objExpression.getType().toString();
        storeAsTypeWithSignaturesImport(strType, objIndex, objTypesCollection);
    }

    /** Will resolve self-assignments */
    private static void resolveSelfAssignmentExpression(SelfAssignmentExpression objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
        String strType = objExpression.getType().toString();
        storeAsTypeWithSignaturesImport(strType, objIndex, objTypesCollection);
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
    private static void resolveBinaryExpression (BinaryExpression objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
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

        storeAsTypeWithSignaturesImport(objExpression.getType().toString(), objIndex, objTypesCollection);
    }

    /** Resolve type casting expressions */
    private static void resolveUnaryExpression (UnaryExpression objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
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

        storeAsTypeWithSignaturesImport(objExpression.getType().toString(), objIndex, objTypesCollection);
    }

    /** Will resolve constants references */
    private static void resolveConstantReference (ConstantReference objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
        if (ExpressionSemanticUtil.isBoolean(objExpression)) {
            objTypesCollection.add(Types.strBoolean);
            return;
        }

        if (PhpLangUtil.isNull(objExpression)) {
            objTypesCollection.add(Types.strNull);
            return;
        }

        storeAsTypeWithSignaturesImport(objExpression.getType().toString(), objIndex, objTypesCollection);
    }

    /** Will resolve ternary operator */
    private static void resolveTernaryOperator (TernaryExpression objExpression, PhpIndex objIndex, LinkedList<String> objTypesCollection) {
        if (null != objExpression.getTrueVariant()) {
            resolveExpressionType(objExpression.getTrueVariant(), objIndex, objTypesCollection);
        }

        if (null != objExpression.getFalseVariant()) {
            resolveExpressionType(objExpression.getFalseVariant(), objIndex, objTypesCollection);
        }
    }
}
