package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

final public class GenerateAlternativeFromArrayKeyExistsStrategy {
    @Nullable
    static public String generate(@NotNull TernaryExpression expression) {
        /* handle inverted cases */
        PsiElement callCandidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
        boolean isInverted       = false;
        if (callCandidate instanceof UnaryExpression) {
            final PsiElement operator = ((UnaryExpression) callCandidate).getOperation();
            if (null != operator && PhpTokenTypes.opNOT == operator.getNode().getElementType()) {
                isInverted    = true;
                callCandidate = ((UnaryExpression) callCandidate).getValue();
            }
        }

        /* verify condition structure */
        final FunctionReference call = OpenapiTypesUtil.isFunctionReference(callCandidate) ? (FunctionReference) callCandidate : null;
        final String functionName    = null == call ? null : call.getName();
        if (null == functionName || 2 != call.getParameters().length || !functionName.equals("array_key_exists")) {
            return null;
        }

        /* array_key_exists is valid only with null-alternatives */
        PsiElement alternative = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
        alternative            = ExpressionSemanticUtil.getExpressionTroughParenthesis(alternative);
        PsiElement value       = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
        value                  = ExpressionSemanticUtil.getExpressionTroughParenthesis(value);
        if (!(value instanceof ArrayAccessExpression) || !PhpLanguageUtil.isNull(alternative)) {
            return null;
        }

        /* verify condition and variant structure */
        final PsiElement[] params         = call.getParameters();
        final ArrayAccessExpression array = (ArrayAccessExpression) value;
        final PsiElement container        = array.getValue();
        final PsiElement index            = null == array.getIndex() ? null : array.getIndex().getValue();
        if (null == params[0] || null == params[1] || null == container || null == index ) {
            return null;
        }
        if (
            !OpenapiEquivalenceUtil.areEqual(params[1], container) ||
            !OpenapiEquivalenceUtil.areEqual(params[0], index)
        ) {
            return null;
        }

        return value.getText() + " ?? " + alternative.getText();
    }
}
