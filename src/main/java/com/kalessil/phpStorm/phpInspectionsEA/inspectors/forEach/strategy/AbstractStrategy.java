package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public abstract class AbstractStrategy {
    static protected boolean isArrayElement(@NotNull PsiElement candidate, @NotNull PsiElement source, @NotNull PsiElement index) {
        if (candidate instanceof ArrayAccessExpression) {
            final ArrayAccessExpression access = (ArrayAccessExpression) candidate;
            final PsiElement base              = access.getValue();
            final ArrayIndex keyHolder         = access.getIndex();
            final PsiElement key               = keyHolder == null ? null : keyHolder.getValue();
            if (base != null && key != null) {
                return OpenapiEquivalenceUtil.areEqual(base, source) && OpenapiEquivalenceUtil.areEqual(key, index);
            }
        }
        return false;
    }

    static protected boolean isArrayValue(@NotNull PsiElement candidate, @NotNull PsiElement value) {
        return OpenapiEquivalenceUtil.areEqual(candidate, value);
    }
}
