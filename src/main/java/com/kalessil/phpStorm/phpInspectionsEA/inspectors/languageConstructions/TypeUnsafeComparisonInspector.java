package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
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
    private static final String patternHarden                = "Please consider using more strict '%o%' here (hidden types casting will not be applied anymore).";
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
                        holder.registerProblem(subject, messageCompareStrict, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CompareStrictFix(targetOperator));

                        return;
                    }
                }

                /* some of objects supporting direct comparison: search for .compare_objects in PHP sources */
                if (ComparableCoreClassesStrategy.apply(left, right, holder)) {
                    return;
                }

                final String messageHarden = patternHarden.replace("%o%", targetOperator);
                holder.registerProblem(subject, messageHarden, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }

    private class CompareStrictFix implements LocalQuickFix {
        final private String operator;

        @NotNull
        @Override
        public String getName() {
            return "Apply strict comparison";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        CompareStrictFix(@NotNull String operator) {
            super();
            this.operator = operator;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof BinaryExpression) {
                final PsiElement operation   = ((BinaryExpression) expression).getOperation();
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, operator);
                if (null != operation && null != replacement) {
                    operation.replace(replacement);
                }
            }
        }
    }
}