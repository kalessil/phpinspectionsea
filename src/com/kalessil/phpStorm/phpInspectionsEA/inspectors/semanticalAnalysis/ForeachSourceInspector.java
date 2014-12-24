package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ForeachSourceInspector extends BasePhpInspection {
    private static final String strProblemResolvingClassSlotType = "Could not resolve this source type, method annotations needed";
    private static final String strProblemResolvingParameterType = "Could not resolve this source type, parameter annotations needed";
    private static final String strProblemResolvingClassSlot = "Could not resolve this source type, annotations needed";
    private static final String strProblemDescription = "This loop does not loop";

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

                String[] arrTypes = null;
                if (objSource instanceof Variable) {
                    arrTypes = this.lookupType(((Variable) objSource).getSignature(), objSource);
                }
                if (objSource instanceof FieldReference) {
                    arrTypes = this.lookupType(((FieldReference) objSource).getSignature(), objSource);
                }
                if (objSource instanceof MethodReference) {
                    arrTypes = this.lookupType(((MethodReference) objSource).getSignature(), objSource);
                }
                if (objSource instanceof FunctionReference && !(objSource instanceof MethodReference)) {
                    arrTypes = this.lookupType(((FunctionReference) objSource).getSignature(), objSource);
                }

                if (null != arrTypes) {
                    /** debug warnings */
                    String strTypes = "";
                    String strGlue = "";
                    for (String strType : arrTypes) {
                        if (strType.equals("")) {
                            continue;
                        }

                        strTypes += strGlue + strType;
                        strGlue = "|";
                    }

                    /** fits for top-level analysis, except it's still strings manipulation */
                    if (strTypes.equals("array")) {
                        return;
                    }

                    holder.registerProblem(objSource, "types extracted: " + strTypes, ProblemHighlightType.WEAK_WARNING);
                }
            }

            @Nullable
            private String[] lookupType (String strSignature, PsiElement objTargetExpression) {
                if (null == strSignature || strSignature.equals("")) {
                    return null;
                }

                /** skip looking up into variable assignment / function */
                if (
                    strSignature.startsWith("#V") ||
                    strSignature.startsWith("#F")
                ) {
                    /** TODO: lookup assignments for types extraction, pipe check */
                    return null;
                }

                /** problem referenced to arguments */
                if (strSignature.startsWith("#A")) {
                    holder.registerProblem(objTargetExpression, strProblemResolvingParameterType, ProblemHighlightType.ERROR);
                    return null;
                }

                /** lookup base types */
                if (strSignature.startsWith("#C")) {
                    if (strSignature.contains("|")) {
                        /** TODO: remove error logging */
                        holder.registerProblem(objTargetExpression, "pipe in type: " + strSignature, ProblemHighlightType.ERROR);
                        return null;
                    }
                    /** classes, simple types */
                    if (!strSignature.matches(".+\\\\(array|\\w+\\[\\])")) {
                        return new String[] { strSignature.replace("#C\\", "") };
                    }

                    return new String[] { "array" };
                }

                /** lookup for property or method */
                final boolean isProperty = strSignature.startsWith("#P");
                final boolean isMethod = strSignature.startsWith("#M");
                if (isProperty || isMethod) {
                    if (strSignature.contains("|")) {
                        /** TODO: remove error logging */
                        holder.registerProblem(objTargetExpression, "pipe in slot: " + strSignature, ProblemHighlightType.ERROR);
                        return null;
                    }

                    String[] arrParts = strSignature.split("#C\\\\");
                    if (arrParts.length != 2) {
                        /** lookup failed - eg reflection */
                        holder.registerProblem(objTargetExpression, strProblemResolvingClassSlot, ProblemHighlightType.ERROR);
                        return null;
                    }

                    arrParts = arrParts[1].split("\\.");
                    if (arrParts.length != 2) {
                        /** TODO: resolve chain type */
                        //holder.registerProblem(objTargetExpression, "2+ call chain: " + strSignature, ProblemHighlightType.ERROR);
                        return null;
                    }

                    String strClassName = arrParts[0];
                    String strSlotName  = arrParts[1];
                    String strAllTypes = "";

                    Collection<PhpClass> objClasses = PhpIndex.getInstance(holder.getProject()).getClassesByName(strClassName);
                    /** class is not resolved */
                    if (objClasses.size() == 0) {
                        return null;
                    }

                    boolean isSlotResolved = false;
                    /** resolve the slot */
                    for (PhpClass objClass : objClasses) {
                        if (isMethod) {
                            for (Method objMethod : objClass.getMethods()) {
                                if (objMethod.getName().equals(strSlotName)) {
                                    isSlotResolved = true;
                                    strAllTypes += "|" + objMethod.getType().toStringRelativized("\\");
                                }
                            }
                        }

                        if (isProperty) {
                            for (Field objField : objClass.getFields()) {
                                if (objField.getName().equals(strSlotName)) {
                                    isSlotResolved = true;
                                    strAllTypes += "|" + objField.getType().toStringRelativized("\\");
                                }
                            }
                        }
                    }
                    /** slot is not resolved */
                    if (!isSlotResolved) {
                        return null;
                    }

                    if(strAllTypes.contains("?")) {
                        holder.registerProblem(objTargetExpression, strProblemResolvingClassSlotType, ProblemHighlightType.ERROR);
                        return null;
                    }

                    /** typeOrClass[] => array */
                    String[] arrSlotTypes = strAllTypes.split("\\|");
                    for (int i = 0; i < arrSlotTypes.length; ++i) {
                        if (arrSlotTypes[i].contains("[]")) {
                            arrSlotTypes[i] = "array";
                        }
                    }
                    return arrSlotTypes;
                }

                /** TODO: remove error logging */
                holder.registerProblem(objTargetExpression, "not handled: " + strSignature, ProblemHighlightType.ERROR);
                return null;
            }
        };
    }
}
