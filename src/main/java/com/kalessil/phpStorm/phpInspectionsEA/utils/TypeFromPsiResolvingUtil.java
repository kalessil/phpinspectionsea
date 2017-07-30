package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.apache.commons.lang.StringUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final public class TypeFromPsiResolvingUtil {

    /** adds type, handling | and #, invoking signatures resolving */
    private static void storeAsTypeWithSignaturesImport(String typeToImport, @Nullable Function scope, @NotNull PhpIndex index, @NotNull Set<String> typesSet) {
        if (typeToImport.contains("|")) {
            for (String oneType : typeToImport.split("\\|")) {
                storeAsTypeWithSignaturesImport(Types.getType(oneType), scope, index, typesSet);
            }
            return;
        }

        if (StringUtils.isEmpty(typeToImport) || typeToImport.equals("?")) {
            return;
        }

        if (typeToImport.contains("#")) {
            TypeFromSignatureResolvingUtil.resolveSignature(typeToImport, scope, index, typesSet);
            return;
        }

        typesSet.add(Types.getType(typeToImport));
    }

    /** high-level resolving logic */
    public static void resolveExpressionType(PsiElement objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        objSubjectExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objSubjectExpression);

        if (objSubjectExpression instanceof ArrayCreationExpression) {
            objTypesSet.add(Types.strArray);

            checkCallables((ArrayCreationExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
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
            if (
                !StringUtils.isEmpty(strVariableName) && strVariableName.charAt(0) == '_' &&
                ExpressionCostEstimateUtil.predefinedVars.contains(strVariableName)
            ) {
                storeAsTypeWithSignaturesImport(Types.strArray, objScope, objIndex, objTypesSet);
                return;
            }
//
//            PsiElement var = ((Variable) objSubjectExpression).resolve();
//            if (var instanceof Variable && var.getParent() instanceof AssignmentExpression) {
//                AssignmentExpression decl = (AssignmentExpression) var.getParent();
//                if (decl.getValue() instanceof  ArrayCreationExpression) {
//                    checkCallables((ArrayCreationExpression) decl.getValue(), objScope, objIndex, objTypesSet);
//                }
//            }

            /* try with signature */
            storeAsTypeWithSignaturesImport(((Variable) objSubjectExpression).getSignature(), objScope, objIndex, objTypesSet);
            /* try with PS itself, makes e.g. parameters resolves happen */
            if (objTypesSet.isEmpty()) {
                for (String resolvedType : ((Variable) objSubjectExpression).getType().filterUnknown().getTypes()) {
                    objTypesSet.add(Types.getType(resolvedType));
                }
            }
            return;
        }


        if (objSubjectExpression instanceof ArrayAccessExpression) {
            PsiElement var = ((ArrayAccessExpression) objSubjectExpression).getValue();
            if (var instanceof PsiReference) {
                var = ((PsiReference) var).resolve();
            }
            if ((var instanceof PhpTypedElement) && ((PhpTypedElement) var).getType().equals(PhpType.STRING)) {
                objTypesSet.add(Types.strString);
            }
            storeAsTypeWithSignaturesImport(((ArrayAccessExpression) objSubjectExpression).getType().toString(), objScope, objIndex, objTypesSet);
            return;
        }


        if (objSubjectExpression instanceof NewExpression) {
            resolveNewExpression((NewExpression) objSubjectExpression, objTypesSet);
            return;
        }

        // resolve reference
        if (objSubjectExpression instanceof PsiReference) {
            PsiElement target = ((PsiReference) objSubjectExpression).resolve();
            if (target instanceof PhpTypedElement) {
                storeAsTypeWithSignaturesImport(((PhpTypedElement) target).getType().toString(), objScope, objIndex, objTypesSet);
                return;
            }
        }

        // fallback if not resolved
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

        /* lambda/anonymous function*/
        if (objSubjectExpression instanceof Function && ((Function) objSubjectExpression).isClosure()) {
            objTypesSet.add(Types.strCallable);
            return;
        }

        if (objSubjectExpression instanceof PhpExpression) {
            resolvePhpExpression((PhpExpression) objSubjectExpression, objScope, objIndex, objTypesSet);
        }

        /* TODO: check which case is not worked out */
    }

    private static void checkCallables(ArrayCreationExpression objSubjectExpression, @Nullable Function objScope, PhpIndex objIndex, HashSet<String> objTypesSet) {
        final PsiElement[] children = objSubjectExpression.getChildren();
        if ((children.length == 2) && (children[0] instanceof PhpPsiElement) && (children[1] instanceof PhpPsiElement)) {
            HashSet<String> itemMethodType = new HashSet<>();
            resolveExpressionType(((PhpPsiElement) children[1]).getFirstPsiChild(), objScope, objIndex, itemMethodType);
            if (!itemMethodType.contains(Types.strString)) {
                return;
            }

            HashSet<String> itemClassType = new HashSet<>();
            resolveExpressionType(((PhpPsiElement) children[0]).getFirstPsiChild(), objScope, objIndex, itemClassType);
            if (!itemClassType.contains(Types.strString)) {
                boolean isObject = false;
                for (final String type : itemClassType) {
                    if (type.charAt(0) == '\\') {
                        isObject = true;
                        break;
                    }
                }
                if (!isObject) {
                    return;
                }
            }

            objTypesSet.add(Types.strCallable);
        }
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
        if (PhpLanguageUtil.isBoolean(objSubjectExpression)) {
            objTypesSet.add(Types.strBoolean);
            return;
        }

        if (PhpLanguageUtil.isNull(objSubjectExpression)) {
            objTypesSet.add(Types.strNull);
            return;
        }

        final String types;
        final Collection<? extends PhpNamedElement> declaration = objSubjectExpression.resolveGlobal(false);
        if (declaration.size() > 0) {
            types = declaration.iterator().next().getType().toString();
        } else {
            types = objSubjectExpression.getType().toString();
        }

        storeAsTypeWithSignaturesImport(types, objScope, objIndex, objTypesSet);
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
