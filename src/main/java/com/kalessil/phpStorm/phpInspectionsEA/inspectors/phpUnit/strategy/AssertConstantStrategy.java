package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertConstantStrategy {
    private final static Set<String> targetConstants       = new HashSet<>();
    private final static Map<String, String> targetMapping = new HashMap<>();
    static {
        targetConstants.add("null");
        targetConstants.add("true");
        targetConstants.add("false");

        targetMapping.put("assertSame",    "assert%s");
        targetMapping.put("assertNotSame", "assertNot%s");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 1) {
                for (final PsiElement argument : arguments) {
                    if (argument instanceof ConstantReference) {
                        final String constantName = ((ConstantReference) argument).getName();
                        if (constantName != null) {
                            final String constantNameNormalized = constantName.toLowerCase();
                            if (targetConstants.contains(constantNameNormalized)) {
                                final String suggestedAssertion = String.format(
                                        targetMapping.get(methodName),
                                        StringUtils.capitalize(constantNameNormalized)
                                );
                                final String[] suggestedArguments = new String[arguments.length - 1];
                                suggestedArguments[0] = Arrays.stream(arguments)
                                        .filter(a -> a != argument)
                                        .findFirst().get().getText();
                                if (arguments.length > 2) {
                                    suggestedArguments[1] = arguments[2].getText();
                                }
                                holder.registerProblem(
                                        reference,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                        new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                                );

                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
