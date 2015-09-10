package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromSignatureResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ForeachSourceInspector extends BasePhpInspection {
    private static PhpClass objTraversable = null;
    private static final String strAlreadyHandled = "\\already-handled";
    private static final String strClassNotResolved= "\\class-not-resolved";

    private static final String strProblemResolvingDeclaration = "Could not resolve this source type, instance of " +
            "declaration. The following is not resolved: ";
    private static final String strProblemResolvingIsEmpty = "Could not resolve this source type, ensure it's type is annotated";
    private static final String strProblemResolvingMixed = "Could not resolve this source type, specify possible types instead of mixed";
    private static final String strProblemResolvingArrayItemType = "Could not resolve this source type, array item type annotation needed";
    private static final String strProblemResolvingParameterType = "Could not resolve this source type, parameter type annotation needed";
    private static final String strProblemResolvingClassSlot = "Could not resolve this source type, expression type annotation needed";
    private static final String strProblemDescription = "This source type can not be iterated: given ";

    @NotNull
    public String getShortName() {
        return "ForeachSourceInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                this.inspectSource(foreach.getArray());
            }

            private void inspectSource(PsiElement objSource) {
                objSource = ExpressionSemanticUtil.getExpressionTroughParenthesis(objSource);
                if (null == objSource) {
                    return;
                }

                /** skip arrays-related operations */
                if (
                    objSource instanceof ArrayAccessExpression ||
                    objSource instanceof ArrayCreationExpression
                ) {
                    return;
                }

                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());

                boolean isExpressionInspected = false;
                LinkedList<String> listSignatureTypes = new LinkedList<String>();
                //noinspection ConstantConditions
                if (!isExpressionInspected && objSource instanceof FieldReference) {
                    this.lookupType(((FieldReference) objSource).getSignature(), objSource, listSignatureTypes, objIndex);
                    isExpressionInspected = true;
                }
                if (!isExpressionInspected && objSource instanceof MethodReference) {
                    this.lookupType(((MethodReference) objSource).getSignature(), objSource, listSignatureTypes, objIndex);
                    isExpressionInspected = true;
                }
                if (!isExpressionInspected && objSource instanceof Variable) {
                    this.lookupType(((Variable) objSource).getSignature(), objSource, listSignatureTypes, objIndex);
                    isExpressionInspected = true;
                }
                if (!isExpressionInspected && objSource instanceof FunctionReference) {
                    this.lookupType(((FunctionReference) objSource).getSignature(), objSource, listSignatureTypes, objIndex);
                    isExpressionInspected = true;
                }
                if (!isExpressionInspected) {
                    /** something what is not supported */
                    return;
                }

                if (listSignatureTypes.size() == 0) {
                    /** resolving failed at all */
                    holder.registerProblem(objSource, strProblemResolvingIsEmpty, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                this.analyseTypesProvided(objSource, listSignatureTypes, objIndex);
            }


            /**
             * Will check semantics for resolved types information
             */
            private void analyseTypesProvided(PsiElement objTargetExpression, List<String> listSignatureTypes, PhpIndex objIndex) {
                for (String strType : listSignatureTypes) {
                    if (
                        strType.equals("\\array") ||
                        strType.endsWith("[]") ||
                        strType.equals("\\Traversable")
                    ) {
                        continue;
                    }


                    if (strType.equals(strClassNotResolved) || strType .equals(strAlreadyHandled)) {
                        continue;
                    }


                    if (strType.equals("\\mixed")) {
                        if (listSignatureTypes.size() == 1) {
                            holder.registerProblem(objTargetExpression, strProblemResolvingMixed, ProblemHighlightType.WEAK_WARNING);
                            return;
                        }
                        continue;
                    }


                    if (
                        strType.equals("\\null")  ||
                        strType.equals("\\bool")  ||
                        strType.equals("\\void")  ||
                        strType.equals("\\int")   ||
                        strType.equals("\\float") ||
                        strType.equals("\\string")
                    ) {
                        if (listSignatureTypes.size() == 1) {
                            holder.registerProblem(objTargetExpression, strProblemDescription + strType, ProblemHighlightType.ERROR);
                        } else {
                            holder.registerProblem(objTargetExpression, strProblemDescription + strType + " possibly not handled", ProblemHighlightType.WEAK_WARNING);
                        }
                        return;
                    }


                    /** lookup class/interface and check \Traversable support */
                    Collection<PhpClass> objClasses = PhpIndexUtil.getObjectInterfaces(strType, objIndex);
                    if (objClasses.size() > 0) {
                        if (null == objTraversable) {
                            objTraversable = objIndex.getInterfacesByFQN("\\Traversable").iterator().next();
                        }

                        for (PhpClass objClass : objClasses) {
                            /** TODO: not clear why, but isSuperClass receives a null on VCS commit */
                            if (null == objClass || null == objTraversable) {
                                continue;
                            }

                            String strTraversableFQN = objTraversable.getFQN();
                            /**
                             * PhpClassHierarchyUtils.isSuperClass not handling interfaces, so scan complete inheritance tree
                             */
                            HashSet<PhpClass> interfaces = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(objClass, false);
                            for (PhpClass oneInterface : interfaces) {
                                String strFQN = oneInterface.getFQN();
                                if (null != strFQN && strFQN.equals(strTraversableFQN)) {
                                    interfaces.clear();
                                    objClasses.clear();
                                    return;
                                }
                            }
                            interfaces.clear();
                        }
                        objClasses.clear();

                        holder.registerProblem(objTargetExpression, strProblemDescription + strType, ProblemHighlightType.ERROR);
                        return;
                    } else {
                        holder.registerProblem(objTargetExpression, strProblemResolvingDeclaration + strType, ProblemHighlightType.ERROR);
                        return;
                    }
                }
            }

            /**
             * Will get back types information out of signature and put stubs where further lookup
             * is not possible/implemented yet
             */
            private void lookupType (String strSignature, PsiElement objTargetExpression, LinkedList<String> listSignatureTypes, PhpIndex objIndex) {

                /** re-dispatch or terminate lookup */
                if (StringUtil.isEmpty(strSignature)) {
                    return;
                }
                if (strSignature.contains("|")) {
                    for (String strSignaturePart : strSignature.split("\\|")) {
                        this.lookupType(strSignaturePart, objTargetExpression, listSignatureTypes, objIndex);
                    }
                    return;
                }

                if (strSignature.charAt(0) != '#' && strSignature.charAt(0) != '?') {
                    if (strSignature.charAt(0) != '\\') {
                        strSignature = "\\" + strSignature;
                    }
                    listSignatureTypes.add(strSignature);

                    return;
                }

                char charTypeOfSignature = (strSignature.length() >= 2 ? strSignature.charAt(1) : '?');

                /** skip looking up into variable assignment / function */
                if (charTypeOfSignature == 'V' || charTypeOfSignature == '?') {
                    /** TODO: lookup assignments for types extraction, un-mark as handled */
                    //? => holder.registerProblem(objTargetExpression, strProblemResolvingClassSlotType, ProblemHighlightType.ERROR);
                    listSignatureTypes.add(strAlreadyHandled);
                    return;
                }

                if (charTypeOfSignature == 'F') {
                    Collection<Function> objFunctionsCollection = objIndex.getFunctionsByName(strSignature.replace("#F", ""));
                    for (Function objFunction : objFunctionsCollection) {
                        lookupType(objFunction.getType().toString(), objTargetExpression, listSignatureTypes, objIndex);
                    }
                    objFunctionsCollection.clear();

                    return;
                }

                /** problem referenced to arguments */
                if (charTypeOfSignature == 'A') {
                    listSignatureTypes.add(strAlreadyHandled);
                    holder.registerProblem(objTargetExpression, strProblemResolvingParameterType, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
                /** problem referenced to array item */
                if (charTypeOfSignature == 'E') {
                    listSignatureTypes.add(strAlreadyHandled);
                    holder.registerProblem(objTargetExpression, strProblemResolvingArrayItemType, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
                /** lookup core types and classes */
                if (charTypeOfSignature == 'C') {
                    String strTypeExtracted = strSignature.replace("#C", "");
                    listSignatureTypes.add(strTypeExtracted);
                    return;
                }


                /** === more complex analysis - classes are here === */
                /** lookup for property or method */
                final boolean isProperty = (charTypeOfSignature == 'P');
                final boolean isMethod   = (charTypeOfSignature == 'M');
                final boolean isConstant = (charTypeOfSignature == 'K');
                if (isProperty || isMethod || isConstant) {
                    /** check signature structure */
                    String[] arrParts = strSignature.split("#C");
                    if (arrParts.length != 2) {
                        /** lookup failed - eg not recognizable behind reflection */
                        listSignatureTypes.add(strAlreadyHandled);
                        holder.registerProblem(objTargetExpression, strProblemResolvingClassSlot, ProblemHighlightType.ERROR);
                        return;
                    }

                    arrParts = arrParts[1].split("\\.");
                    int intLeftItemsToProcess = arrParts.length;
                    String strResolvedType = null;
                    for (String strSlot : arrParts) {
                        --intLeftItemsToProcess;

                        if (null == strResolvedType) {
                            strResolvedType = strSlot;
                            continue;
                        }

                        HashSet<String> listResolvedTypes = TypeFromSignatureResolvingUtil.resolveSlot(strResolvedType, strSlot, objIndex);
                        strResolvedType = listResolvedTypes.iterator().next();

                        /** break on poly-variants and missing classes */
                        if (
                            (intLeftItemsToProcess > 0 && listResolvedTypes.size() > 1) ||
                            strResolvedType.equals(strClassNotResolved)
                        ) {
                            strResolvedType = strClassNotResolved;

                            listResolvedTypes.clear();
                            break;
                        }

                        listResolvedTypes.clear();
                    }

                    this.lookupType(strResolvedType, objTargetExpression, listSignatureTypes, objIndex);
                }
            }
        };
    }
}
