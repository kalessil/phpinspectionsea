package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NestedTernaryOperatorInspector extends BasePhpInspection {
    private static final String messageNested            = "Nested ternary operator should not be used (maintainability issues).";
    private static final String messagePriorities        = "This may not work as expected (wrap condition into '()' to specify intention).";
    private static final String messageVariantsIdentical = "True and false variants are identical, most probably this is a bug.";

    private static final Set<IElementType> safeOperations = new HashSet<>();
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
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                /* Case 1: nested ternary operators */
                final PsiElement condition
                        = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof TernaryExpression) {
                    holder.registerProblem(condition, messageNested, ProblemHighlightType.WEAK_WARNING);
                }
                final PsiElement trueVariant
                        = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                if (trueVariant instanceof TernaryExpression) {
                    holder.registerProblem(trueVariant, messageNested, ProblemHighlightType.WEAK_WARNING);
                }
                final PsiElement falseVariant          = expression.getFalseVariant();
                final PsiElement extractedFalseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(falseVariant);
                if (extractedFalseVariant instanceof TernaryExpression) {
                    final boolean allow =
                            expression.isShort() && falseVariant instanceof TernaryExpression &&
                            ((TernaryExpression) falseVariant).isShort();
                    if (!allow) {
                        holder.registerProblem(extractedFalseVariant, messageNested, ProblemHighlightType.WEAK_WARNING);
                    }
                }

                /* Case 2: identical variants */
                if (
                    trueVariant != null && extractedFalseVariant != null &&
                    OpeanapiEquivalenceUtil.areEqual(trueVariant, extractedFalseVariant)
                ) {
                    holder.registerProblem(expression, messageVariantsIdentical, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 3: operations which might produce a value as not expected */
                if (condition instanceof BinaryExpression && !(expression.getCondition() instanceof ParenthesizedExpression)) {
                    final IElementType operationType = ((BinaryExpression) condition).getOperationType();
                    if (operationType != null && !safeOperations.contains(operationType)) {
                        holder.registerProblem(condition, messagePriorities, ProblemHighlightType.WEAK_WARNING);
                    }
                }

                /* Case 4: literal operators priorities issue */
                final PsiElement parent = expression.getParent();
                if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    if (binary.getRightOperand() == expression && PhpTokenTypes.tsLIT_OPS.contains(binary.getOperationType())) {
                        holder.registerProblem(binary, messagePriorities, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}