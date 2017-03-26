package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ClassInStringContextStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ComparableCoreClassesStrategy;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    private static final String messageHarden                = "Hardening to type safe '===', '!==' will cover/point to types casting issues.";
    private static final String patternCompareStrict         = "Safely use '%o%' here.";
    private static final String messageToStringMethodMissing = "Class %class% must implement __toString().";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeComparisonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                /* verify operation is as expected */
                final PsiElement operation  = expression.getOperation();
                final IElementType operator = null == operation ? null : operation.getNode().getElementType();
                if (PhpTokenTypes.opEQUAL == operator || PhpTokenTypes.opNOT_EQUAL == operator) {
                    this.triggerProblem(expression, operator);
                }
            }

            private void triggerProblem(@NotNull final BinaryExpression subject, @NotNull final IElementType operator) {
                final String targetOperator = PhpTokenTypes.opEQUAL == operator ? "===" : "!==";
                final PsiElement left       = subject.getLeftOperand();
                final PsiElement right      = subject.getRightOperand();
                if (right instanceof StringLiteralExpression || left instanceof StringLiteralExpression) {
                    final PsiElement nonStringOperand;
                    final String literalValue;
                    if (right instanceof StringLiteralExpression) {
                        literalValue     = ((StringLiteralExpression) right).getContents();
                        nonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(left);
                    } else {
                        literalValue     = ((StringLiteralExpression) left).getContents();
                        nonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(right);
                    }

                    /* resolve 2nd operand type, if class ensure __toString is implemented */
                    if (ClassInStringContextStrategy.apply(nonStringOperand, holder, subject, messageToStringMethodMissing)) {
                        /* TODO: weak warning regarding under-the-hood string casting */
                        return;
                    }

                    /* string literal is numeric, no strict compare possible */
                    if (!literalValue.matches("^[0-9+-]+$")) {
                        final String messageCompareStrict = patternCompareStrict.replace("%o%", targetOperator);
                        holder.registerProblem(subject, messageCompareStrict, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        return;
                    }
                }

                /* some of objects supporting direct comparison: search for .compare_objects in PHP sources */
                if (ComparableCoreClassesStrategy.apply(left, right, holder)) {
                    return;
                }

                holder.registerProblem(subject, messageHarden, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}