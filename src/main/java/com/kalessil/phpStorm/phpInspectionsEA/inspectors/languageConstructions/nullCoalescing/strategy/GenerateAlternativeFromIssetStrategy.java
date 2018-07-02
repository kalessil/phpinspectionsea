package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

final public class GenerateAlternativeFromIssetStrategy {
    @Nullable
    static public String generate(@NotNull TernaryExpression expression) {
        /* handle inverted cases */
        PsiElement issetCandidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
        boolean isInverted        = false;
        if (issetCandidate instanceof UnaryExpression) {
            final PsiElement operator = ((UnaryExpression) issetCandidate).getOperation();
            if (null != operator && PhpTokenTypes.opNOT == operator.getNode().getElementType()) {
                isInverted     = true;
                issetCandidate = ((UnaryExpression) issetCandidate).getValue();
            }
        }

        /* verify condition structure */
        final PhpIsset isset = issetCandidate instanceof PhpIsset ? (PhpIsset) issetCandidate : null;
        if (null == isset || 1 != isset.getVariables().length) {
            return null;
        }

        /* verify subject match and alternative availability */
        final PsiElement subject = ExpressionSemanticUtil.getExpressionTroughParenthesis(isset.getVariables()[0]);
        PsiElement value         = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
        value                    = ExpressionSemanticUtil.getExpressionTroughParenthesis(value);
        PsiElement alternative   = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
        alternative              = ExpressionSemanticUtil.getExpressionTroughParenthesis(alternative);
        if (
            null == subject || null == value || null == alternative ||
            !OpenapiEquivalenceUtil.areEqual(subject, value)
        ) {
            return null;
        }

        return subject.getText() + " ?? " + alternative.getText();
    }
}