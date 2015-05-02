package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class AlterInForeachInspector extends BasePhpInspection {
    private static final String strProblemDescription     = "Can be refactored as '$%c% = ...' if $%v% is defined as reference (ensure that array supplied)";
    private static final String strProblemUnsafeReference = "This variable must be unset just after foreach to prevent possible side-effects";
    private static final String strProblemKeyReference    = "Provokes PHP Fatal error (Key element cannot be a reference)";
    private static final String strProblemAmbiguousUnset  = "This unset is not really needed as value not a reference";

    @NotNull
    public String getShortName() {
        return "AlterInForeachInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            public void visitPhpForeach(ForeachStatement foreach) {
                /* lookup for reference preceding value */
                Variable objForeachValue = foreach.getValue();
                if (null != objForeachValue) {
                    PsiElement prevElement = objForeachValue.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (null != prevElement) {
                        PhpPsiElement nextExpression = foreach.getNextPsiSibling();

                        if (PhpTokenTypes.opBIT_AND == prevElement.getNode().getElementType()) {
                            /* === requested by the community, not part of original idea  === */
                            /* look up for parents which doesn't have following statements, eg #53 */
                            PsiElement foreachParent = foreach.getParent();
                            while (null == nextExpression && null != foreachParent) {
                                if (!(foreachParent instanceof GroupStatement)) {
                                    nextExpression = ((PhpPsiElement) foreachParent).getNextPsiSibling();
                                }
                                foreachParent = foreachParent.getParent();

                                if (null == foreachParent || foreachParent instanceof Function || foreachParent instanceof PhpFile) {
                                    break;
                                }
                            }
                            /* === requested by the community, not part of original idea  === */

                            /* allow return after loop - no issues can be introduced */
                            boolean isRequirementFullFilled = false;
                            if (null == nextExpression || nextExpression instanceof PhpReturn) {
                                isRequirementFullFilled = true;
                            }
                            /* check unset is applied to value-variable */
                            if (nextExpression instanceof PhpUnset) {
                                PhpPsiElement[] unsetArguments = ((PhpUnset) nextExpression).getArguments();
                                if (1 == unsetArguments.length && unsetArguments[0] instanceof Variable) {
                                    String unsetArgumentName =  unsetArguments[0].getName();
                                    String foreachValueName  =  objForeachValue.getName();
                                    if (
                                        !StringUtil.isEmpty(unsetArgumentName) && !StringUtil.isEmpty(foreachValueName) &&
                                        unsetArgumentName.equals(foreachValueName)
                                    ) {
                                        isRequirementFullFilled = true;
                                    }
                                }
                            }

                            /* check if warning needs to be reported */
                            if (!isRequirementFullFilled) {
                                holder.registerProblem(objForeachValue, strProblemUnsafeReference, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        } else {
                            /* check if un-sets non-reference value - not needed at all, probably forgotten to cleanup */
                            if (nextExpression instanceof PhpUnset) {
                                PhpPsiElement[] unsetArguments = ((PhpUnset) nextExpression).getArguments();
                                if (1 == unsetArguments.length && unsetArguments[0] instanceof Variable) {
                                    String unsetArgumentName =  unsetArguments[0].getName();
                                    String foreachValueName  =  objForeachValue.getName();
                                    if (
                                        !StringUtil.isEmpty(unsetArgumentName) && !StringUtil.isEmpty(foreachValueName) &&
                                        unsetArgumentName.equals(foreachValueName)
                                    ) {
                                        holder.registerProblem(nextExpression, strProblemAmbiguousUnset, ProblemHighlightType.WEAK_WARNING);
                                    }
                                }
                            }
                        }
                    }
                }

                /* lookup for reference preceding key */
                Variable objForeachKey = foreach.getKey();
                if (null != objForeachKey) {
                    PsiElement prevElement = objForeachKey.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (null != prevElement && PhpTokenTypes.opBIT_AND == prevElement.getNode().getElementType()) {
                        holder.registerProblem(prevElement, strProblemKeyReference, ProblemHighlightType.ERROR);
                    }
                }
            }

            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PhpPsiElement objOperand = assignmentExpression.getVariable();
                if (!(objOperand instanceof ArrayAccessExpression)) {
                    return;
                }

                ArrayAccessExpression objContainer = (ArrayAccessExpression) objOperand;
                if (
                    null == objContainer.getIndex() ||
                    null == objContainer.getValue() ||
                    !(objContainer.getIndex().getValue() instanceof Variable)
                ) {
                    return;
                }


                PhpPsiElement objForeachSourceCandidate = objContainer.getValue();
                PhpPsiElement objForeachKeyCandidate = objContainer.getIndex().getValue();


                PsiElement objParent = assignmentExpression.getParent();
                while (null != objParent && !(objParent instanceof PhpFile)) {
                    /* terminate if reached callable */
                    if (objParent instanceof Function) {
                        return;
                    }

                    if (objParent instanceof ForeachStatement) {
                        ForeachStatement objForeach = (ForeachStatement) objParent;
                        Variable objForeachValue = objForeach.getValue();
                        if (
                            null != objForeachValue &&
                            null != objForeach.getKey() &&
                            null != objForeach.getArray() &&
                            PsiEquivalenceUtil.areElementsEquivalent(objForeach.getKey(), objForeachKeyCandidate) &&
                            PsiEquivalenceUtil.areElementsEquivalent(objForeach.getArray(), objForeachSourceCandidate)
                        ) {
                            String strName = objForeachValue.getName();
                            if (null != strName) {
                                String strMessage = strProblemDescription
                                        .replace("%c%", strName)
                                        .replace("%v%", strName);
                                holder.registerProblem(objOperand, strMessage, ProblemHighlightType.WEAK_WARNING);

                                return;
                            }
                        }
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }
}