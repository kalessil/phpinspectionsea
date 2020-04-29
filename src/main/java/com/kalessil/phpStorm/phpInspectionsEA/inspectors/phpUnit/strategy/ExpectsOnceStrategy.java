package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ExpectsOnceStrategy {
    private final static String message = "'->once()' would make more sense here.";

    static public void apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        if (methodName.equals("expects")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 1 && arguments[0] instanceof MethodReference) {
                final MethodReference innerReference = (MethodReference) arguments[0];
                final String innerMethodName         = innerReference.getName();
                if (innerMethodName != null && innerMethodName.equals("exactly")) {
                    final PsiElement[] innerArguments = innerReference.getParameters();
                    if (innerArguments.length == 1 && OpenapiTypesUtil.isNumber(innerArguments[0])) {
                        final boolean isResult = innerArguments[0].getText().equals("1");
                        if (isResult) {
                            holder.registerProblem(
                                    innerReference,
                                    MessagesPresentationUtil.prefixWithEa(message),
                                    new PhpUnitAssertFixer("once", new String[]{})
                            );
                        }
                    }
                }
            }
        }
    }
}
