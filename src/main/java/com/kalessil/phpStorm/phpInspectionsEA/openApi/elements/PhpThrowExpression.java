package com.kalessil.phpStorm.phpInspectionsEA.openApi.elements;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StatementWithArgument;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

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
        // PS 2020.3, 2021.1 has changed the throw structure, hence we have to rely on low-level structures.
        final Optional<PsiElement> argument = Stream.of(expression, expression.getFirstChild())
                .filter(e -> OpenapiTypesUtil.is(e.getFirstChild(), PhpTokenTypes.kwTHROW))
                .findFirst();
        if (argument.isPresent()) {
            final PsiElement expression = argument.get();
            if (expression instanceof PhpPsiElement) {
                return ((PhpPsiElement) expression).getFirstPsiChild();
            }
        }
        return null;
    }

    @NotNull
    public PhpPsiElement getExpression() {
        return expression instanceof StatementWithArgument
                ? expression
                : (PhpPsiElement) expression.getParent();
    }
}
