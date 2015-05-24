package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class NestedTernaryOperatorInspector extends BasePhpInspection {
    private static final String strProblemNested            = "Nested ternary operator shall not be used";
    private static final String strProblemPriorities        = "Inspect this operation, perhaps it works not as expected";
    private static final String strProblemVariantsIdentical = "True and false variants are identical";

    @NotNull
    public String getShortName() {
        return "NestedTernaryOperatorInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                /* check for nested TO cases */
                PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (objCondition instanceof TernaryExpression) {
                    holder.registerProblem(objCondition, strProblemNested, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                PsiElement objTrueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                if (objTrueVariant instanceof TernaryExpression) {
                    holder.registerProblem(objTrueVariant, strProblemNested, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                PsiElement objFalseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                if (objFalseVariant instanceof TernaryExpression) {
                    holder.registerProblem(objFalseVariant, strProblemNested, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                /* check if return variants identical */
                if (null != objTrueVariant && null != objFalseVariant && PsiEquivalenceUtil.areElementsEquivalent(objTrueVariant, objFalseVariant)) {
                    holder.registerProblem(expression, strProblemVariantsIdentical, ProblemHighlightType.GENERIC_ERROR);
                }

                /* check for confusing condition */
                if (objCondition instanceof BinaryExpression && !(expression.getCondition() instanceof ParenthesizedExpression)) {
                    PsiElement operation       = ((BinaryExpression) objCondition).getOperation();
                    IElementType operationType = null;
                    if (null != operation) {
                        operationType = operation.getNode().getElementType();
                    }

                    /* don't report easily recognized cases */
                    if (
                            PhpTokenTypes.opAND              != operationType &&
                            PhpTokenTypes.opOR               != operationType &&
                            PhpTokenTypes.opIDENTICAL        != operationType &&
                            PhpTokenTypes.opNOT_IDENTICAL    != operationType &&
                            PhpTokenTypes.opEQUAL            != operationType &&
                            PhpTokenTypes.opNOT_EQUAL        != operationType &&
                            PhpTokenTypes.opGREATER          != operationType &&
                            PhpTokenTypes.opGREATER_OR_EQUAL != operationType &&
                            PhpTokenTypes.opLESS             != operationType &&
                            PhpTokenTypes.opLESS_OR_EQUAL    != operationType &&
                            PhpTokenTypes.kwINSTANCEOF       != operationType
                    ) {
                        holder.registerProblem(objCondition, strProblemPriorities, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}