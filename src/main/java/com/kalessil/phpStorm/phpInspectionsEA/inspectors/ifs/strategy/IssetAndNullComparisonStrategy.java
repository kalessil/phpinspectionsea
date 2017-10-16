package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class IssetAndNullComparisonStrategy {
    private final static String messagePattern = "It seems like '%e%' is already covered by 'isset(...)'.";

    static public boolean apply(@NotNull List<PsiElement> conditions, @NotNull ProblemsHolder holder) {
        /* first ensure that we have null identity checks at all */
        final Map<PsiElement, PsiElement> nullTestSubjects = new HashMap<>();
        for (final PsiElement oneCondition : conditions) {
            if (oneCondition instanceof BinaryExpression) {
                final BinaryExpression expression = (BinaryExpression) oneCondition;

                /* we need only !== and === operations */
                final IElementType operator = expression.getOperationType();
                if (operator != PhpTokenTypes.opIDENTICAL && operator != PhpTokenTypes.opNOT_IDENTICAL) {
                    continue;
                }

                /* quickly check if any operands is a constant */
                final PsiElement left  = expression.getLeftOperand();
                final PsiElement right = expression.getRightOperand();
                if (!(left instanceof ConstantReference) && !(right instanceof ConstantReference)) {
                    continue;
                }

                /* store null test subjects */
                if (PhpLanguageUtil.isNull(right)) {
                    if (null != left) {
                        nullTestSubjects.put(expression, left);
                    }
                    continue;
                }
                if (PhpLanguageUtil.isNull(left)) {
                    if (null != right) {
                        nullTestSubjects.put(expression, right);
                    }
                    // continue;
                }
            }
        }
        if (0 == nullTestSubjects.size()) {
            return false;
        }

        boolean hasReportedExpressions = false;
        for (final PsiElement oneCondition : conditions) {
            /* do not process null identity checks */
            if (nullTestSubjects.containsKey(oneCondition)) {
                continue;
            }

            /* unwrap ! and () */
            PsiElement issetCandidate = oneCondition;
            if (issetCandidate instanceof UnaryExpression) {
                final PsiElement notOperatorCandidate = ((UnaryExpression) issetCandidate).getOperation();
                if (null != notOperatorCandidate && notOperatorCandidate.getNode().getElementType() == PhpTokenTypes.opNOT) {
                    PsiElement invertedValue = ((UnaryExpression) issetCandidate).getValue();
                    invertedValue = ExpressionSemanticUtil.getExpressionTroughParenthesis(invertedValue);
                    if (null == invertedValue) {
                        continue;
                    }

                    issetCandidate = invertedValue;
                }
            }
            if (!(issetCandidate instanceof PhpIsset) || 0 == ((PhpIsset) issetCandidate).getVariables().length) {
                continue;
            }

            /* process isset constructions */
            for (final PsiElement issetArgument : ((PhpIsset) issetCandidate).getVariables()) {
                /* compare with know null identity checked subjects */
                for (final Map.Entry<PsiElement, PsiElement> nullTestPair : nullTestSubjects.entrySet()) {
                    if (!OpeanapiEquivalenceUtil.areEqual(nullTestPair.getValue(), issetArgument)) {
                        continue;
                    }

                    hasReportedExpressions = true;

                    final PsiElement nullTestExpression = nullTestPair.getKey();
                    final String message                = messagePattern.replace("%e%", nullTestExpression.getText());
                    holder.registerProblem(nullTestExpression, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        }
        nullTestSubjects.clear();

        return hasReportedExpressions;
    }
}
