package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class AlterInForeachInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "Can be refactored as '$%c% = ...' if $%v% is defined as reference";
    private static final String strProblemUnsafeReference = "This variable must be unset just after foreach to prevent possible side-effects";

    @NotNull
    public String getShortName() {
        return "AlterInForeachInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            public void visitPhpForeach(ForeachStatement foreach) {
                /** lookup for reference preceding value */
                Variable objForeachValue = foreach.getValue();
                if (
                    null != objForeachValue &&
                    objForeachValue.getPrevSibling().getNode().getElementType() == PhpTokenTypes.opBIT_AND
                ) {
                    /** test if it's immediately unset after foreach */
                    PhpPsiElement nextExpression = foreach.getNextPsiSibling();
                    if (null == nextExpression || !(nextExpression instanceof PhpUnset)) {
                        holder.registerProblem(objForeachValue, strProblemUnsafeReference, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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
                    /** terminate if reached callable */
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