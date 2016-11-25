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

import java.util.HashSet;

public class NestedTernaryOperatorInspector extends BasePhpInspection {
    private static final String messageNested            = "Nested ternary operator should not be used (maintainability issues)";
    private static final String messagePriorities        = "This may not work as expected (wrap condition into '()' to specify intention)";
    private static final String messageVariantsIdentical = "True and false variants are identical, most probably this is a bug";

    private static final HashSet<IElementType> safeOperations = new HashSet<>();
    static {
        safeOperations.add(PhpTokenTypes.opAND);
        safeOperations.add(PhpTokenTypes.opOR);
        safeOperations.add(PhpTokenTypes.opIDENTICAL);
        safeOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        safeOperations.add(PhpTokenTypes.opEQUAL);
        safeOperations.add(PhpTokenTypes.opNOT_EQUAL);
        safeOperations.add(PhpTokenTypes.opGREATER);
        safeOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        safeOperations.add(PhpTokenTypes.opLESS);
        safeOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);
        safeOperations.add(PhpTokenTypes.kwINSTANCEOF);
    }

    @NotNull
    public String getShortName() {
        return "NestedTernaryOperatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                /* Case 1: nested ternary operators */
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

                /* Case 2: identical variants */
                if (null != trueVariant && null != falseVariant && PsiEquivalenceUtil.areElementsEquivalent(trueVariant, falseVariant)) {
                    holder.registerProblem(expression, messageVariantsIdentical, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 3: operations which might produce a value as not expected */
                if (condition instanceof BinaryExpression && !(expression.getCondition() instanceof ParenthesizedExpression)) {
                    final PsiElement operation = ((BinaryExpression) condition).getOperation();
                    IElementType operationType = null;
                    if (null != operation) {
                        operationType = operation.getNode().getElementType();
                    }

                    if (null != operationType && !safeOperations.contains(operationType)) {
                        holder.registerProblem(condition, messagePriorities, ProblemHighlightType.WEAK_WARNING);
                    }
                }

                /* Case 4: literal operators priorities issue */
                if (expression.getParent() instanceof BinaryExpression) {
                    BinaryExpression parent = (BinaryExpression) expression.getParent();
                    if (parent.getRightOperand() == expression) {
                        final PsiElement parentOperation = parent.getOperation();
                        IElementType operationType = null;
                        if (null != parentOperation) {
                            operationType = parentOperation.getNode().getElementType();
                        }

                        if (operationType == PhpTokenTypes.opLIT_AND || operationType == PhpTokenTypes.opLIT_OR) {
                            holder.registerProblem(parent, messagePriorities, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }
        };
    }
}