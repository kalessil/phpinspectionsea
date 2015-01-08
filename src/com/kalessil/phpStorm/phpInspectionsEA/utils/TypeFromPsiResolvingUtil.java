package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class TypeFromPsiResolvingUtil {

    /** adds type, handling | and #, invoking signatures resolving */
    private static void storeAsTypeWithSignaturesImport(String strTypeToImport, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        if (strTypeToImport.contains("|")) {
            for (String strOneType : strTypeToImport.split("\\|")) {
                storeAsTypeWithSignaturesImport(Types.getType(strOneType), objScope, objIndex, objTypesSet);
            }
            return;
        }

        if (StringUtil.isEmpty(strTypeToImport) || strTypeToImport.equals("?")) {
            return;
        }

        if (strTypeToImport.contains("#")) {
            TypeFromSignatureResolvingUtil.resolveSignature(strTypeToImport, objScope, objIndex, objTypesSet);
            return;
        }

        objTypesSet.add(Types.getType(strTypeToImport));
    }

    /** high-level resolving logic */
    public static void resolveExpressionType(PsiElement objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        objSubjectExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objSubjectExpression);

        if (objSubjectExpression instanceof ArrayCreationExpression) {
            objTypesSet.add(Types.strArray);
            return;
        }
        if (objSubjectExpression instanceof StringLiteralExpression) {
            objTypesSet.add(Types.strString);
            return;
        }
        if (objSubjectExpression instanceof ConstantReference) {
            resolveConstantReference((ConstantReference) objSubjectExpression, objScope, objIndex, objTypesSet);
            return;
        }

        if (objSubjectExpression instanceof TernaryExpression) {
            resolveTernaryOperator((TernaryExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof UnaryExpression) {
            resolveUnaryExpression((UnaryExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof BinaryExpression) {
            resolveBinaryExpression((BinaryExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof SelfAssignmentExpression) {
            resolveSelfAssignmentExpression((SelfAssignmentExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
            return;
        }


        if (objSubjectExpression instanceof Variable) {
            String strVariableName = ((Variable) objSubjectExpression).getName();
            if (null != strVariableName && strVariableName.charAt(0) == '_') {
                if ("|_GET|_POST|_SESSION|_REQUEST|_FILES|_COOKIE|_ENV|_SERVER|".contains("|" + strVariableName + "|")) {
                    storeAsTypeWithSignaturesImport(Types.strArray, objScope, objIndex, objTypesSet);
                    return;
                }

            }

            storeAsTypeWithSignaturesImport(((Variable) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof ArrayAccessExpression) {
            storeAsTypeWithSignaturesImport(((ArrayAccessExpression) objSubjectExpression).getType().toString(), objScope, objIndex, objTypesSet);
            return;
        }


        if (objSubjectExpression instanceof NewExpression) {
            resolveNewExpression((NewExpression) objSubjectExpression, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof ClassConstantReference) {
            storeAsTypeWithSignaturesImport(((ClassConstantReference) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof FieldReference) {
            storeAsTypeWithSignaturesImport(((FieldReference) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof MethodReference) {
            storeAsTypeWithSignaturesImport(((MethodReference) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            return;
        }
        if (objSubjectExpression instanceof FunctionReference) {
            storeAsTypeWithSignaturesImport(((FunctionReference) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            return;
        }


        if (objSubjectExpression instanceof PhpExpression) {
            resolvePhpExpression((PhpExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
        }

        /** TODO: check which case is not worked out */
    }

    /** resolve numbers and exotic structures, eg list() = .... */
    private static void resolvePhpExpression(PhpExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        storeAsTypeWithSignaturesImport(objSubjectExpression.getType().toString(), objScope, objIndex, objTypesSet);
    }

    /** Will resolve self-assignments */
    private static void resolveSelfAssignmentExpression(SelfAssignmentExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        storeAsTypeWithSignaturesImport(objSubjectExpression.getType().toString(), objScope, objIndex, objTypesSet);
    }

    /** Will resolve type of new expression */
    private static void resolveNewExpression(NewExpression objSubjectExpression, HashSet<String> objTypesSet) {
        ClassReference objClassRef = objSubjectExpression.getClassReference();
        if (null == objClassRef || null == objClassRef.getFQN()) {
            objTypesSet.add(Types.strResolvingAbortedOnPsiLevel);
            return;
        }

        objTypesSet.add(objClassRef.getFQN());
    }

    /** resolve some of binary expressions . | && | || */
    private static void resolveBinaryExpression (BinaryExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        PsiElement objOperation = objSubjectExpression.getOperation();
        if (null == objOperation) {
            return;
        }

        IElementType objOperationType = objOperation.getNode().getElementType();
        if (objOperationType == PhpTokenTypes.opCONCAT) {
            objTypesSet.add(Types.strString);
            return;
        } else if (objOperationType == PhpTokenTypes.opAND || objOperationType == PhpTokenTypes.opOR) {
            objTypesSet.add(Types.strBoolean);
            return;
        }

        storeAsTypeWithSignaturesImport(objSubjectExpression.getType().toString(), objScope, objIndex, objTypesSet);
    }

    /** Resolve type casting expressions */
    private static void resolveUnaryExpression (UnaryExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        PsiElement objOperation = objSubjectExpression.getOperation();
        if (null == objOperation) {
            return;
        }

        IElementType objType = objOperation.getNode().getElementType();
        if (PhpTokenTypes.CAST_OPERATORS.contains(objType)) {
            //opOBJECT_CAST, opUNSET_CAST, opBINARY_CAST are out of interest
            if (objType == PhpTokenTypes.opINTEGER_CAST) {
                objTypesSet.add(Types.strInteger);
            } else if (objType == PhpTokenTypes.opARRAY_CAST) {
                objTypesSet.add(Types.strArray);
            } else if (objType == PhpTokenTypes.opBOOLEAN_CAST) {
                objTypesSet.add(Types.strBoolean);
            } else if (objType == PhpTokenTypes.opFLOAT_CAST) {
                objTypesSet.add(Types.strFloat);
            } else if (objType == PhpTokenTypes.opSTRING_CAST) {
                objTypesSet.add(Types.strString);
            }

            return;
        }

        storeAsTypeWithSignaturesImport(objSubjectExpression.getType().toString(), objScope, objIndex, objTypesSet);
    }

    /** Will resolve constants references */
    private static void resolveConstantReference (ConstantReference objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        if (ExpressionSemanticUtil.isBoolean(objSubjectExpression)) {
            objTypesSet.add(Types.strBoolean);
            return;
        }

        if (PhpLangUtil.isNull(objSubjectExpression)) {
            objTypesSet.add(Types.strNull);
            return;
        }

        storeAsTypeWithSignaturesImport(objSubjectExpression.getType().toString(), objScope, objIndex, objTypesSet);
    }

    /** Will resolve ternary operator */
    private static void resolveTernaryOperator (TernaryExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        if (null != objSubjectExpression.getTrueVariant()) {
            resolveExpressionType(objSubjectExpression.getTrueVariant(), objScope, objIndex, objTypesSet);
        }

        if (null != objSubjectExpression.getFalseVariant()) {
            resolveExpressionType(objSubjectExpression.getFalseVariant(), objScope, objIndex, objTypesSet);
        }
    }
}
