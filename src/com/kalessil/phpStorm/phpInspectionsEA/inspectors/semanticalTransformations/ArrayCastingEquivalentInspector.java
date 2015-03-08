package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;


import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class ArrayCastingEquivalentInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Probably '(array) ...' construction shall be used (applying to null, stdClass will change behavior)";
    private static final String strIsArray = "is_array";

    @NotNull
    public String getShortName() {
        return "ArrayCastingEquivalentInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If expression) {
                if (ExpressionSemanticUtil.hasAlternativeBranches(expression)){
                    return;
                }

                /** body has only assignment, which to be extracted */
                GroupStatement objBody = ExpressionSemanticUtil.getGroupStatement(expression);
                if (null == objBody || ExpressionSemanticUtil.countExpressionsInGroup(objBody) != 1) {
                    return;
                }
                PsiElement objAction = ExpressionSemanticUtil.getLastStatement(objBody);
                objAction = (null == objAction ? null : objAction.getFirstChild());
                if (!(objAction instanceof AssignmentExpression)) {
                    return;
                }

                /** expecting !function(...) in condition */
                PsiElement objConditionExpression = null;
                IElementType objOperation = null;
                if (expression.getCondition() instanceof UnaryExpression) {
                    UnaryExpression objCondition = (UnaryExpression) expression.getCondition();
                    objOperation = objCondition.getOperation().getNode().getElementType();
                    objConditionExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objCondition.getValue());
                }
                if (objOperation != PhpTokenTypes.opNOT || !(objConditionExpression instanceof FunctionReference)) {
                    return;
                }

                /** inspect expression */
                AssignmentExpression objAssignment = (AssignmentExpression) objAction;
                PsiElement objTrueVariant = objAssignment.getVariable();
                PsiElement objFalseVariant = objAssignment.getValue();
                if (
                    null != objTrueVariant && null != objFalseVariant &&
                    this.isArrayCasting((FunctionReference) objConditionExpression, objTrueVariant, objFalseVariant)
                ) {
                    holder.registerProblem(expression.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            /** expecting !function(...), true and false expressions */
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                PsiElement objTrueVariant = expression.getTrueVariant();
                PsiElement objFalseVariant = expression.getFalseVariant();

                if (null != objTrueVariant && null != objFalseVariant) {
                    PsiElement objConditionExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());

                    if (
                        objConditionExpression instanceof FunctionReference &&
                        this.isArrayCasting((FunctionReference) objConditionExpression, objTrueVariant, objFalseVariant)
                    ) {
                        holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }

            private boolean isArrayCasting(@NotNull FunctionReference objCondition, @NotNull PsiElement objTrue, @NotNull PsiElement objFalse) {
                /** false variant should be array creation */
                if (!(objFalse instanceof ArrayCreationExpression)) {
                    return false;
                }

                /** condition expected to be is_array(arg) */
                String strFunctionName = objCondition.getName();
                if (
                    objCondition.getParameters().length != 1 ||
                    StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals(strIsArray)
                ) {
                    return false;
                }

                /** extract array values, expected one value only */
                LinkedList<PsiElement> valuesSet = new LinkedList<PsiElement>();
                for (PsiElement objChild : objFalse.getChildren()) {
                    if (objChild instanceof PhpPsiElement) {
                        valuesSet.add(objChild.getFirstChild());
                    }
                }
                //noinspection SimplifiableIfStatement
                if (valuesSet.size() != 1) {
                    return false;
                }

                PsiElement firstValue = valuesSet.getFirst();
                valuesSet.clear();
                return
                    PsiEquivalenceUtil.areElementsEquivalent(objTrue, objCondition.getParameters()[0]) &&
                    PsiEquivalenceUtil.areElementsEquivalent(objTrue, firstValue);
            }
        };
    }
}
