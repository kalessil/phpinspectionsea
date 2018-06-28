package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StringNormalizationInspector extends BasePhpInspection {
    private static final String patternInvertedNesting  = "'%s' makes more sense here.";
    private static final String patternSenselessNesting = "'%s(...)' makes no sense here.";

    @NotNull
    public String getShortName() {
        return "StringNormalizationInspection";
    }

    private static final Set<String> lengthManipulation    = new HashSet<>();
    private static final Set<String> caseManipulation      = new HashSet<>();
    private static final Set<String> innerCaseManipulation = new HashSet<>();
    static {
        innerCaseManipulation.add("strtolower");
        innerCaseManipulation.add("strtoupper");
        innerCaseManipulation.add("mb_convert_case");
        innerCaseManipulation.add("mb_strtolower");
        innerCaseManipulation.add("mb_strtoupper");

        caseManipulation.addAll(innerCaseManipulation);
        caseManipulation.add("ucfirst");
        caseManipulation.add("lcfirst");
        caseManipulation.add("ucwords");

        lengthManipulation.add("ltrim");
        lengthManipulation.add("rtrim");
        lengthManipulation.add("trim");
        lengthManipulation.add("substr");
        lengthManipulation.add("mb_substr");
    }

    final static private Pattern regexTrimmedCharacters;
    static {
        regexTrimmedCharacters = Pattern.compile("(['\"]).*\\p{L}.*\\1");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0 && OpenapiTypesUtil.isFunctionReference(arguments[0])) {
                        final FunctionReference innerCall = (FunctionReference) arguments[0];
                        final String innerCallName        = innerCall.getName();
                        if (innerCallName != null) {
                            final PsiElement[] innerArguments = innerCall.getParameters();
                            if (innerArguments.length > 0) {
                                if (lengthManipulation.contains(functionName) && caseManipulation.contains(innerCallName)) {
                                    final boolean isTarget =
                                        !functionName.endsWith("trim") ||
                                        arguments.length == 1 ||
                                        (arguments[1] instanceof  StringLiteralExpression && !regexTrimmedCharacters.matcher(arguments[1].getText()).matches());
                                    if (isTarget) {
                                        final String theString    = innerArguments[0].getText();
                                        final String newInnerCall = reference.getText().replace(arguments[0].getText(), theString);
                                        final String replacement  = innerCall.getText().replace(theString, newInnerCall);
                                        final String message      = String.format(patternInvertedNesting, replacement);
                                        holder.registerProblem(reference, message, new NormalizationFix(replacement));
                                    }
                                } else if (caseManipulation.contains(functionName) && caseManipulation.contains(innerCallName)) {
                                    if (functionName.equals(innerCallName)) {
                                        final String message = String.format(patternSenselessNesting, innerCallName);
                                        holder.registerProblem(innerCall, message, new NormalizationFix(innerArguments[0].getText()));
                                    } else if (!innerCaseManipulation.contains(innerCallName)) {
                                        /* false-positives: ucwords with 2 arguments */
                                        final boolean isTarget = !innerCallName.equals("ucwords") || innerArguments.length == 1;
                                        if (isTarget) {
                                            final String message = String.format(patternSenselessNesting, innerCallName);
                                            holder.registerProblem(innerCall, message, new NormalizationFix(innerArguments[0].getText()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class NormalizationFix extends UseSuggestedReplacementFixer {
        private static final String title = "Fix the string normalization";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        NormalizationFix(@NotNull String expression) {
            super(expression);
        }
    }
}
