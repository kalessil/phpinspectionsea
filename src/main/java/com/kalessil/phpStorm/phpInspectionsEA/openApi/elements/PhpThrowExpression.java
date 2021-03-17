package com.kalessil.phpStorm.phpInspectionsEA.openApi.elements;

import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StatementWithArgument;
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

final public class PhpThrowExpression {
    @NotNull
    private final PhpPsiElement expression;

    public PhpThrowExpression(@NotNull PhpPsiElement expression){
        this.expression = expression;
    }

    @Nullable
    public PhpPsiElement getArgument() {
        return expression.getFirstPsiChild();
    }

    @NotNull
    public PhpPsiElement getExpression() {
        return expression instanceof StatementWithArgument
                ? expression
                : (PhpPsiElement) expression.getParent();
    }
}
