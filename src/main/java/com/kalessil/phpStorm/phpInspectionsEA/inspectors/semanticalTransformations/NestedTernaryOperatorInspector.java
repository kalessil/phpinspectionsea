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
    private static final String messageNested            = "Nested ternary operator should not be used (maintainability issues)";
    private static final String messagePriorities        = "Inspect this operation, it may not work as expected (priorities issues)";
    private static final String messageVariantsIdentical = "True and false variants are identical";

    @NotNull
    public String getShortName() {
        return "NestedTernaryOperatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                /* check for nested TO cases */
                PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof TernaryExpression) {
                    holder.registerProblem(condition, messageNested, ProblemHighlightType.WEAK_WARNING);
                }
                PsiElement trueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                if (trueVariant instanceof TernaryExpression) {
                    holder.registerProblem(trueVariant, messageNested, ProblemHighlightType.WEAK_WARNING);
                }
                PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                if (falseVariant instanceof TernaryExpression) {
                    holder.registerProblem(falseVariant, messageNested, ProblemHighlightType.WEAK_WARNING);
                }

                /* check if return variants identical */
                if (null != trueVariant && null != falseVariant && PsiEquivalenceUtil.areElementsEquivalent(trueVariant, falseVariant)) {
                    holder.registerProblem(expression, messageVariantsIdentical, ProblemHighlightType.GENERIC_ERROR);
                }

                /* check for confusing condition */
                if (condition instanceof BinaryExpression && !(expression.getCondition() instanceof ParenthesizedExpression)) {
                    final PsiElement operation = ((BinaryExpression) condition).getOperation();
                    IElementType operationType = null;
                    if (null != operation) {
                        operationType = operation.getNode().getElementType();
                    }

                    /* don't report easily recognized cases */
                    if (
                            PhpTokenTypes.opAND              != operationType &&
                            PhpTokenTypes.opLIT_AND          != operationType &&
                            PhpTokenTypes.opOR               != operationType &&
                            PhpTokenTypes.opLIT_OR           != operationType &&
                            PhpTokenTypes.opIDENTICAL        != operationType &&
                            PhpTokenTypes.opNOT_IDENTICAL    != operationType &&
                            PhpTokenTypes.opEQUAL            != operationType &&
                            PhpTokenTypes.opNOT_EQUAL        != operationType &&
                            PhpTokenTypes.opGREATER          != operationType &&
                            PhpTokenTypes.opGREATER_OR_EQUAL != operationType &&
                            PhpTokenTypes.opLESS             != operationType &&
                            PhpTokenTypes.opLESS_OR_EQUAL    != operationType
                    ) {
                        holder.registerProblem(condition, messagePriorities, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}