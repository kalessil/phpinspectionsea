package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertStringEqualsFileStrategy {

    private final static Set<String> validContexts    = new HashSet<>();
    private final static Set<String> targetAssertions = new HashSet<>();
    static {
        targetAssertions.add("assertSame");
        targetAssertions.add("assertEquals");

        validContexts.add("assertFileEquals");
        validContexts.add("assertStringEqualsFile");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetAssertions.contains(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 1 && OpenapiTypesUtil.isFunctionReference(assertionArguments[0])) {
                final FunctionReference candidate = (FunctionReference) assertionArguments[0];
                final String functionName         = candidate.getName();
                if (functionName != null && functionName.equals("file_get_contents")) {
                    final PsiElement[] functionArguments = candidate.getParameters();
                    if (functionArguments.length == 1) {
                        final Function scope       = ExpressionSemanticUtil.getScope(reference);
                        final boolean shouldReport = scope == null || !validContexts.contains(scope.getName());
                        if (shouldReport) {
                            final String[] suggestedArguments = new String[assertionArguments.length];
                            suggestedArguments[0] = functionArguments[0].getText();
                            suggestedArguments[1] = assertionArguments[1].getText();
                            if (assertionArguments.length > 2) {
                                suggestedArguments[2] = assertionArguments[2].getText();
                            }
                            final String suggestedAssertion = "assertStringEqualsFile";
                            holder.registerProblem(
                                    reference,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                    new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                            );

                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }
}