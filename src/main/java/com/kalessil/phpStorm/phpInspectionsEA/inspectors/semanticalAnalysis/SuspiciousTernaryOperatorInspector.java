package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

public class SuspiciousTernaryOperatorInspector extends BasePhpInspection {
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
        return "SuspiciousTernaryOperatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                final PsiElement condition    = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                final PsiElement trueVariant  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());

                /* Case 1: identical variants */
                if (trueVariant != null && falseVariant != null && OpenapiEquivalenceUtil.areEqual(trueVariant, falseVariant)) {
                    holder.registerProblem(expression, messageVariantsIdentical, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 2: operations which might produce a value as not expected */
                if (condition instanceof BinaryExpression && !(expression.getCondition() instanceof ParenthesizedExpression)) {
                    final IElementType operator = ((BinaryExpression) condition).getOperationType();
                    if (operator != null && !safeOperations.contains(operator)) {
                        holder.registerProblem(condition, messagePriorities, ProblemHighlightType.GENERIC_ERROR);
                    }
                }

                /* Case 3: literal operators priorities issue */
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
