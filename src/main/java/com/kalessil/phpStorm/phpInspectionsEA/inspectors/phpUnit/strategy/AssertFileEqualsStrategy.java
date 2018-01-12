package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AssertFileEqualsStrategy {
    private final static String messagePattern = "'%s(...)' would fit more here.";

    private final static Set<String> targetAssertions = new HashSet<>();
    static {
        targetAssertions.add("assertSame");
        targetAssertions.add("assertEquals");
    }

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetAssertions.contains(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 1) {
                /* try extracting file_get_contents arguments */
                final List<PsiElement> extracts = Arrays.stream(assertionArguments)
                    .map(argument -> {
                        PsiElement mappingResult = null;
                        if (OpenapiTypesUtil.isFunctionReference(argument)) {
                            final FunctionReference candidate = (FunctionReference) argument;
                            final String functionName         = candidate.getName();
                            if (functionName != null && functionName.equals("file_get_contents")) {
                                final PsiElement[] functionArguments = candidate.getParameters();
                                if (functionArguments.length == 1) {
                                    mappingResult = functionArguments[0];
                                }
                            }
                        }
                        return mappingResult;
                    })
                    .collect(Collectors.toList());
                /* now check if reporting is needed */
                /* TODO: assertStringEqualsFile(..., file_get_contents()) -> assertFileEquals */
                if (extracts.size() >= 2 && extracts.get(0) != null && extracts.get(1) != null) {
                    final String[] suggestedArguments = new String[assertionArguments.length];
                    suggestedArguments[0] = extracts.get(0).getText();
                    suggestedArguments[1] = extracts.get(1).getText();
                    if (assertionArguments.length > 2) {
                        suggestedArguments[2] = assertionArguments[2].getText();
                    }
                    final String suggestedAssertion = "assertFileEquals";
                    holder.registerProblem(
                            reference,
                            String.format(messagePattern, suggestedAssertion),
                            new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                    );

                    result = true;
                }
                extracts.clear();
            }
        }
        return result;
    }
}