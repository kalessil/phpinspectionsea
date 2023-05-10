package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class WillReturnStrategy {
    private final static String messagePattern = "'->%s(...)' would make more sense here.";

    final private static Map<String, String> methodsMapping = new HashMap<>();
    static {
        methodsMapping.put("returnValueMap", "willReturnMap");
        methodsMapping.put("returnCallback", "willReturnCallback");
        methodsMapping.put("returnValue",    "willReturn");
        methodsMapping.put("returnArgument", "willReturnArgument");
    }

    static public void apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        if (methodName.equals("will")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 1 && arguments[0] instanceof MethodReference) {
                final MethodReference innerReference = (MethodReference) arguments[0];
                final String innerMethodName         = innerReference.getName();
                if (innerMethodName != null && methodsMapping.containsKey(innerMethodName)) {
                    final PsiElement[] innerArguments = innerReference.getParameters();
                    if (innerArguments.length == 1) {
                        final String suggestedAssertion = methodsMapping.get(innerMethodName);
                        holder.registerProblem(
                                reference,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                new PhpUnitAssertFixer(suggestedAssertion, new String[]{innerArguments[0].getText()})
                        );
                    }
                }
            }
        }
    }
}
