package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ForeachSourceInspector extends BasePhpInspection {
    private static PhpClass objTraversable = null;
    private static final String strAlreadyHandled = "\\already-handled";
    private static final String strClassNotResolved= "\\class-not-resolved";

    private static final String strProblemResolvingDeclaration = "Could not resolve this source type, inspire of " +
            "declaration. Following is nor resolved: ";
    private static final String strProblemResolvingIsEmpty = "Could not resolve this source type, ensure it's type annotated at all";
    private static final String strProblemResolvingMixed = "Could not resolve this source type, specify possible types instead of mixed";
    private static final String strProblemResolvingArrayItemType = "Could not resolve this source type, array item type annotation needed";
    private static final String strProblemResolvingClassSlotType = "Could not resolve this source type, method or property type annotation needed";
    private static final String strProblemResolvingParameterType = "Could not resolve this source type, parameter type annotation needed";
    private static final String strProblemResolvingClassSlot = "Could not resolve this source type, expression  type annotation needed";
    private static final String strProblemDescription = "This source type can not be iterated: given ";

    @NotNull
    public String getDisplayName() {
        return "Semantics: foreach source";
    }

    @NotNull
    public String getShortName() {
        return "ForeachSourceInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
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
                    objSource instanceof ArrayCreationExpression ||
                    objSource instanceof ArrayAccessExpression
                ) {
                    return;
                }

                boolean isExpressionInspected = false;
                LinkedList<String> listSignatureTypes = new LinkedList<>();
                if (objSource instanceof Variable) {
                    this.lookupType(((Variable) objSource).getSignature(), objSource, listSignatureTypes);
                    isExpressionInspected = true;
                }
                if (objSource instanceof FieldReference) {
                    this.lookupType(((FieldReference) objSource).getSignature(), objSource, listSignatureTypes);
                    isExpressionInspected = true;
                }
                if (objSource instanceof MethodReference) {
                    this.lookupType(((MethodReference) objSource).getSignature(), objSource, listSignatureTypes);
                    isExpressionInspected = true;
                }
                if (objSource instanceof FunctionReference && !(objSource instanceof MethodReference)) {
                    this.lookupType(((FunctionReference) objSource).getSignature(), objSource, listSignatureTypes);
                    isExpressionInspected = true;
                }
                if (!isExpressionInspected) {
                    /** something what is not supported */
                    return;
                }

                if (listSignatureTypes.size() == 0) {
                    /** resolving failed at all */
                    holder.registerProblem(objSource, strProblemResolvingIsEmpty, ProblemHighlightType.ERROR);
                    return;
                }

                this.analyseTypesProvided(objSource, listSignatureTypes);
                listSignatureTypes.clear();
            }


            /**
             * Will check semantics for resolved types information
             *
             * @param objTargetExpression
             * @param listSignatureTypes
             */
            private void analyseTypesProvided(PsiElement objTargetExpression, LinkedList<String> listSignatureTypes) {
                for (String strType : listSignatureTypes) {
                    if (
                        strType.equals("\\array") ||
                        strType.equals("\\string") ||
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
                        strType.equals("\\null") ||
                        strType.equals("\\bool") ||
                        strType.equals("\\void")
                    ) {
                        if (listSignatureTypes.size() == 1) {
                            holder.registerProblem(objTargetExpression, strProblemDescription + strType, ProblemHighlightType.ERROR);
                        } else {
                            holder.registerProblem(objTargetExpression, strProblemDescription + strType + " possibly not handled", ProblemHighlightType.WEAK_WARNING);
                        }
                        return;
                    }


                    /** lookup class and check \Traversable support */
                    Collection<PhpClass> objClasses = PhpIndex.getInstance(holder.getProject()).getClassesByName(strType);
                    if (objClasses.size() == 0) {
                        objClasses = PhpIndex.getInstance(holder.getProject()).getClassesByFQN(strType);
                    }

                    if (objClasses.size() > 0) {
                        if (null == objTraversable) {
                            objTraversable = PhpIndex.getInstance(holder.getProject()).getClassByName("\\Traversable");
                        }

                        for (PhpClass objClass : objClasses) {
                            if (PhpClassHierarchyUtils.isSuperClass(objTraversable, objClass, true)) {
                                continue;
                            }
                        }

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
             *
             * @param strSignature
             * @param objTargetExpression
             * @param listSignatureTypes
             */
            private void lookupType (String strSignature, PsiElement objTargetExpression, LinkedList<String> listSignatureTypes) {
                /** re-dispatch or terminate lookup */
                if (null == strSignature || strSignature.equals("")) {
                    return;
                }
                if (strSignature.contains("|")) {
                    for (String strSignaturePart : strSignature.split("\\|")) {
                        this.lookupType(strSignaturePart, objTargetExpression, listSignatureTypes);
                    }
                    return;
                }

                /** === early returns=== **/
                /** skip looking up into variable assignment / function */
                if (strSignature.startsWith("#V") || strSignature.startsWith("#F")) {
                    /** TODO: lookup assignments for types extraction, un-mark as handled */
                    listSignatureTypes.add(strAlreadyHandled);
                    return;
                }
                /** problem referenced to arguments */
                if (strSignature.startsWith("#A")) {
                    listSignatureTypes.add(strAlreadyHandled);
                    holder.registerProblem(objTargetExpression, strProblemResolvingParameterType, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
                /** problem referenced to array item */
                if (strSignature.startsWith("#E")) {
                    listSignatureTypes.add(strAlreadyHandled);
                    holder.registerProblem(objTargetExpression, strProblemResolvingArrayItemType, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
                /** lookup core types and classes */
                if (strSignature.startsWith("#C")) {
                    String strTypeExtracted = strSignature.replace("#C", "");
                    listSignatureTypes.add(strTypeExtracted);
                    return;
                }
                /** problem referenced to calls resolving */
                if (strSignature.contains("?")) {
                    listSignatureTypes.add(strAlreadyHandled);
                    holder.registerProblem(objTargetExpression, strProblemResolvingClassSlotType, ProblemHighlightType.ERROR);
                    return;
                }


                /** === more complex analysis - classes are here === */
                /** lookup for property or method */
                final boolean isProperty = strSignature.startsWith("#P");
                final boolean isMethod = strSignature.startsWith("#M");
                if (isProperty || isMethod) {
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

                        strResolvedType = this.lookupChain(strResolvedType, strSlot);
                        /** break on poly-variants and missing classes */
                        if (
                            (intLeftItemsToProcess > 0 && strResolvedType.contains("|")) ||
                            strResolvedType.equals(strClassNotResolved)
                        ) {
                            strResolvedType = strClassNotResolved;
                            break;
                        }
                    }

                    this.lookupType(strResolvedType, objTargetExpression, listSignatureTypes);
                    return;
                }


                /** here passing by types which does not need further handling - they simple ones already */
                if (!strSignature.startsWith("\\")) {
                    strSignature = "\\" + strSignature;
                }
                listSignatureTypes.add(strSignature);
            }

            /**
             * @param strClass to lookup
             * @param strChain to lookup
             * @return String
             */
            private String lookupChain(String strClass, String strChain) {
                Collection<PhpClass> objClasses = PhpIndex.getInstance(holder.getProject()).getClassesByName(strClass);
                if (objClasses.size() == 0) {
                    objClasses = PhpIndex.getInstance(holder.getProject()).getClassesByFQN(strClass);
                }
                /** resolve the slot in known classes */
                if (objClasses.size() > 0) {
                    LinkedList<String> listTypes = new LinkedList<>();

                    for (PhpClass objClass : objClasses) {
                        boolean isSlotFound = false;

                        for (Method objMethod : objClass.getMethods()) {
                            if (objMethod.getName().equals(strChain)) {
                                listTypes.add(objMethod.getType().toString());
                                isSlotFound = true;
                                break;
                            }
                        }
                        if (isSlotFound) {
                            continue;
                        }

                        for (Field objField : objClass.getFields()) {
                            if (objField.getName().equals(strChain)) {
                                listTypes.add(objField.getType().toString());
                                break;
                            }
                        }
                    }

                    /** only option */
                    if (listTypes.size() == 1) {
                        return listTypes.get(0);
                    }

                    /** resolved in several classes - who knows why classes duplicated */
                    List<String> listUniqueSignatures = new ArrayList<>(new HashSet<>(listTypes));
                    listTypes.clear();

                    String strSeparator = "";
                    String strAllTypes = "";
                    for (String strOneItem : listUniqueSignatures) {
                        strAllTypes += strSeparator + strOneItem;
                        strSeparator = "|";
                    }

                    listUniqueSignatures.clear();
                    return strAllTypes;
                }

                return strClassNotResolved;
            }
        };
    }
}
