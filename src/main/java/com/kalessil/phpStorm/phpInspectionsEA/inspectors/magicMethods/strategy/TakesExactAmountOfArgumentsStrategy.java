package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TakesExactAmountOfArgumentsStrategy {
    private static final String messagePattern = "%s accepts exactly %s arguments.";

    static public void apply(int argumentsCount, @NotNull Method method, @NotNull ProblemsHolder holder) {
        if (method.getParameters().length != argumentsCount) {
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
            if (nameNode != null) {
                holder.registerProblem(
                        nameNode,
                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), method.getName(), argumentsCount)
                );
            }
        }
    }
}
