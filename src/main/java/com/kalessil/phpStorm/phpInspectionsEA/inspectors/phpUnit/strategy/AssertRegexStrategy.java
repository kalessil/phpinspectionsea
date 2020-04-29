package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AssertRegexStrategy {
    private final static String messagePattern = "'%s(...)' would fit more here.";

    private final static Map<String, String> numberCompareTargets = new HashMap<>();
    private final static Set<String> binaryTargets = new HashSet<>();
    static {
        binaryTargets.add("assertTrue");
        binaryTargets.add("assertFalse");

        numberCompareTargets.put("assertSame",      "1");
        numberCompareTargets.put("assertNotSame",   "0");
        numberCompareTargets.put("assertEquals",    "1");
        numberCompareTargets.put("assertNotEquals", "0");
    }

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (numberCompareTargets.containsKey(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length >= 2 && OpenapiTypesUtil.isNumber(assertionArguments[0])) {
                final boolean isTarget = OpenapiTypesUtil.isFunctionReference(assertionArguments[1]);
                if (isTarget) {
                    final FunctionReference candidate = (FunctionReference) assertionArguments[1];
                    final String candidateName        = candidate.getName();
                    if (candidateName != null && candidateName.equals("preg_match")) {
                        final PsiElement[] functionArguments = candidate.getParameters();
                        if (functionArguments.length == 2) {
                            final String suggestedAssertion   = assertionArguments[0].getText().equals(numberCompareTargets.get(methodName))
                                    ? "assertRegExp"
                                    : "assertNotRegExp";
                            final String[] suggestedArguments = new String[assertionArguments.length];
                            suggestedArguments[0]             = functionArguments[0].getText();
                            suggestedArguments[1]             = functionArguments[1].getText();
                            if (assertionArguments.length > 2) {
                                suggestedArguments[2] = assertionArguments[2].getText();
                            }
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
        } else if (binaryTargets.contains(methodName)) {
            final PsiElement[] assertionArguments = reference.getParameters();
            if (assertionArguments.length > 0 && assertionArguments[0] instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) assertionArguments[0];
                if (binary.getOperationType() == PhpTokenTypes.opGREATER) {
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    if (OpenapiTypesUtil.isNumber(right) && OpenapiTypesUtil.isFunctionReference(left)) {
                        final boolean isTargetNumber = right.getText().equals("0");
                        if (isTargetNumber) {
                            final FunctionReference candidate = (FunctionReference) left;
                            final String candidateName        = candidate.getName();
                            if (candidateName != null && candidateName.equals("preg_match")) {
                                final PsiElement[] functionArguments = candidate.getParameters();
                                if (functionArguments.length == 2) {
                                    final String suggestedAssertion   = methodName.equals("assertTrue")
                                            ? "assertRegExp"
                                            : "assertNotRegExp";
                                    final String[] suggestedArguments = new String[assertionArguments.length + 1];
                                    suggestedArguments[0]             = functionArguments[0].getText();
                                    suggestedArguments[1]             = functionArguments[1].getText();
                                    if (assertionArguments.length > 1) {
                                        suggestedArguments[2] = assertionArguments[1].getText();
                                    }
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
            }
        }
        return result;
    }
}
