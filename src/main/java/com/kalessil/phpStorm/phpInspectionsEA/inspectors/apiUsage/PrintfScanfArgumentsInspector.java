package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PrintfScanfArgumentsInspector extends BasePhpInspection {
    private static final String messagePattern    = "Pattern seems to be not valid.";
    private static final String messageParameters = "Number of expected parameters is %c%.";

    @NotNull
    @Override
    public String getShortName() {
        return "PrintfScanfArgumentsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "*printf/*scanf arguments count mismatches";
    }

    private static final Map<String, Integer> functions = new HashMap<>();
    static {
        /* pairs function name -> pattern position */
        functions.put("printf",  0);
        functions.put("sprintf", 0);
        functions.put("sscanf",  1);
        functions.put("fprintf", 1);
        functions.put("fscanf",  1);
    }

    final static private Pattern regexPlaceHolders;
    static {
        // raw regex: %((\d+)\$)?[+-]?(?:[ 0]|\\?'.)?-?\d*(?:\.\d*)?[\[sducoxXbgGeEfF]
        regexPlaceHolders = Pattern.compile("%((\\d+|\\*)\\$)?[+-]?(?:[ 0]|\\\\?'.)?-?\\d*(?:\\.\\d*)?[\\[sducoxXbgGeEfF]");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functions.containsKey(functionName)) {
                    return;
                }

                /* resolve needed parameter */
                final int neededPosition              = functions.get(functionName);
                final int minimumArgumentsForAnalysis = neededPosition + 1;
                StringLiteralExpression pattern       = null;
                final PsiElement[] params             = reference.getParameters();
                if (params.length >= minimumArgumentsForAnalysis) {
                    pattern = ExpressionSemanticUtil.resolveAsStringLiteral(params[neededPosition]);
                }
                /* not available */
                if (null == pattern) {
                    return;
                }

                final String content = pattern.getContents().trim();
                if (!content.isEmpty()) {
                    /* find valid placeholders and extract positions specifiers as well */
                    int countWithoutPositionSpecifier = 0;
                    int maxPositionSpecifier          = 0;
                    int countParsedAll                = 0;

                    /* do normalization: %%, inline variables */
                    final String contentAdapted = content.replace("%%", "");
                    final String contentNoVars  = contentAdapted.replaceAll("\\$\\{?\\$?[a-zA-Z0-9]+\\}?", "");
                    if (contentAdapted.length() != contentNoVars.length()) {
                        return;
                    }

                    final Matcher regexMatcher = regexPlaceHolders.matcher(contentAdapted);
                    while (regexMatcher.find()) {
                        ++countParsedAll;

                        if (null != regexMatcher.group(2)) {
                            maxPositionSpecifier = Math.max(maxPositionSpecifier, Integer.parseInt(regexMatcher.group(2)));
                            continue;
                        }

                        ++countWithoutPositionSpecifier;
                    }
                    final int expectedParametersCount = minimumArgumentsForAnalysis + Math.max(countWithoutPositionSpecifier, maxPositionSpecifier);

                    /* check for pattern validity */
                    final int parametersInPattern = StringUtils.countMatches(content.replace("%%", "").replace("%*",""), "%");
                    if (countParsedAll != parametersInPattern) {
                        holder.registerProblem(
                                params[neededPosition],
                                MessagesPresentationUtil.prefixWithEa(messagePattern),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                        return;
                    }

                    /* check for arguments matching */
                    if (expectedParametersCount != params.length) {
                        /* fscanf/sscanf will also return parsed values as an array if no values containers provided */
                        if (params.length == 2) {
                            final boolean returnsArray   = functionName.equals("fscanf") || functionName.equals("sscanf");
                            final PsiElement parent      = returnsArray ? reference.getParent() : null;
                            final PsiElement grandParent = parent == null ? null : parent.getParent();
                            if (returnsArray && parent != null) {
                                /* false-positive: dispatching/deconstructing into containers */
                                if (parent instanceof AssignmentExpression || grandParent instanceof AssignmentExpression) {
                                    return;
                                }
                                /* false-positive: dispatching into calls */
                                else if (parent instanceof ParameterList && grandParent instanceof FunctionReference) {
                                    return;
                                }
                            }
                        }

                        /* false-positives: variadic */
                        if (OpenapiTypesUtil.is(params[params.length - 1].getPrevSibling(), PhpTokenTypes.opVARIADIC)) {
                            return;
                        }
                        /* false-positives: variable modification */
                        final PsiElement argumentWithPattern = params[neededPosition];
                        if (argumentWithPattern instanceof Variable) {
                            final Function function   = ExpressionSemanticUtil.getScope(argumentWithPattern);
                            final GroupStatement body = function == null ? null : ExpressionSemanticUtil.getGroupStatement(function);
                            if (body != null) {
                                for (final Variable candidate : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                    final PsiElement parent  = candidate.getParent();
                                    final boolean isModified = parent instanceof AssignmentExpression &&
                                                               !OpenapiTypesUtil.isAssignment(parent) &&
                                                               candidate == ((AssignmentExpression) parent).getVariable() &&
                                                               OpenapiEquivalenceUtil.areEqual(candidate, argumentWithPattern);
                                    if (isModified) {
                                        return;
                                    }
                                }
                            }
                        }

                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(messageParameters.replace("%c%", String.valueOf(expectedParametersCount))),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                    }
                }
            }
        };
    }
}

