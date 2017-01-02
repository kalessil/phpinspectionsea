package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintfScanfArgumentsInspector extends BasePhpInspection {
    private static final String messagePattern    = "Pattern seems to be not valid.";
    private static final String messageParameters = "Amount of expected parameters is %c%.";

    @NotNull
    public String getShortName() {
        return "PrintfScanfArgumentsInspection";
    }

    private static final HashMap<String, Integer> functions = new HashMap<>();
    static {
        /* pairs function name -> pattern position */
        functions.put("printf",  0);
        functions.put("sprintf", 0);
        functions.put("sscanf",  1);
        functions.put("fprintf", 1);
        functions.put("fscanf",  1);
    }

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexPlaceHolders = null;
    static {
        // raw regex: %((\d+)\$)?[+-]?(?:[ 0]|\\?'.)?-?\d*(?:\.\d+)?[sducoxXbgGeEfF]
        regexPlaceHolders = Pattern.compile("%((\\d+)\\$)?[+-]?(?:[ 0]|\\\\?'.)?-?\\d*(?:\\.\\d+)?[sducoxXbgGeEfF]");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                if (StringUtil.isEmpty(functionName) || !functions.containsKey(functionName)) {
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

                final String content = pattern.getContents();
                if (!StringUtil.isEmpty(content)) {
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
                    final int countParsingExpectedParameters = minimumArgumentsForAnalysis + Math.max(countWithoutPositionSpecifier, maxPositionSpecifier);

                    /* check for pattern validity */
                    final int parametersInPattern = StringUtil.getOccurrenceCount(content.replace("%%", ""), '%');
                    if (countParsedAll != parametersInPattern) {
                        holder.registerProblem(params[neededPosition], messagePattern, ProblemHighlightType.GENERIC_ERROR);
                        return;
                    }

                    /* check for arguments matching */
                    if (countParsingExpectedParameters != params.length) {
                        /* fscanf/sscanf will also return parsed values as an array if no values containers provided */
                        if (2 == params.length) {
                            final boolean returnsArray = functionName.equals("fscanf") || functionName.equals("sscanf");
                            final PsiElement parent    = returnsArray ? reference.getParent() : null;
                            if (
                                returnsArray &&  null != parent &&
                                (parent instanceof AssignmentExpression || parent.getParent() instanceof AssignmentExpression)
                            ) {
                                return;
                            }
                        }

                        final String message = messageParameters.replace("%c%", String.valueOf(countParsingExpectedParameters));
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}

