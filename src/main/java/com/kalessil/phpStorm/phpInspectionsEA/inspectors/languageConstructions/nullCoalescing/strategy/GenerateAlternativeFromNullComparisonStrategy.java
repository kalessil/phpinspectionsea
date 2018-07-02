package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class GenerateAlternativeFromNullComparisonStrategy {
    @Nullable
    static public String generate(@NotNull TernaryExpression expression) {
        /* verify condition structure */
        final PsiElement compareCandidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
        final BinaryExpression compare    = compareCandidate instanceof BinaryExpression ? (BinaryExpression) compareCandidate : null;
        final PsiElement left             = null == compare ? null : compare.getLeftOperand();
        final PsiElement right            = null == compare ? null : compare.getRightOperand();
        final IElementType operation      = null == compare ? null : compare.getOperationType();
        if (
            (operation != PhpTokenTypes.opIDENTICAL && operation != PhpTokenTypes.opNOT_IDENTICAL) ||
            (!PhpLanguageUtil.isNull(left) && !PhpLanguageUtil.isNull(right))
        ) {
            return null;
        }

        /* verify general expression structure */
        final boolean isInverted        = operation == PhpTokenTypes.opNOT_IDENTICAL;
        final PsiElement subject        = PhpLanguageUtil.isNull(right) ? left : right;
        PsiElement value                = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
        value                           = ExpressionSemanticUtil.getExpressionTroughParenthesis(value);
        PsiElement alternative          = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
        alternative                     = ExpressionSemanticUtil.getExpressionTroughParenthesis(alternative);
        if (
            null == subject || null == value || null == alternative ||
            !OpenapiEquivalenceUtil.areEqual(subject, value)
        ) {
            return null;
        }

        return subject.getText() + " ?? " + alternative.getText();
    }
}
