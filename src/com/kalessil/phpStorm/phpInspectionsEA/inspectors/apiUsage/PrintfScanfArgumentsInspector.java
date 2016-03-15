package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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
    private static final String strProblemInvalid     = "Pattern seems to be not valid";
    private static final String strProblemDescription = "Amount of expected parameters is %c%";

    @NotNull
    public String getShortName() {
        return "PrintfScanfArgumentsInspection";
    }

    private static HashMap<String, Integer> functions = null;
    private static HashMap<String, Integer> getFunctions() {
        if (null == functions) {
            /* pairs function name -> pattern position */
            functions = new HashMap<String, Integer>();

            functions.put("printf",  0);
            functions.put("sprintf", 0);
            functions.put("fprintf", 0);

            functions.put("sscanf",  1);
            functions.put("fscanf",  1);
        }

        return functions;
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
                final String strFunctionName = reference.getName();
                HashMap<String, Integer> mapping = getFunctions();
                if (StringUtil.isEmpty(strFunctionName) || !mapping.containsKey(strFunctionName)) {
                    return;
                }

                /* resolve needed parameter */
                final int neededPosition              = mapping.get(strFunctionName);
                final int minimumArgumentsForAnalysis = neededPosition + 1;
                StringLiteralExpression pattern       = null;
                final PsiElement[] referenceParams    = reference.getParameters();
                if (referenceParams.length >= minimumArgumentsForAnalysis) {
                    pattern = ExpressionSemanticUtil.resolveAsStringLiteral(referenceParams[neededPosition]);
                }
                /* not available */
                if (null == pattern) {
                    return;
                }

                String content = pattern.getContents();
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

                    Matcher regexMatcher = regexPlaceHolders.matcher(contentAdapted);
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
                        holder.registerProblem(referenceParams[neededPosition], strProblemInvalid, ProblemHighlightType.GENERIC_ERROR);
                        return;
                    }

                    /* check for arguments matching */
                    if (countParsingExpectedParameters != referenceParams.length) {
                        String strError = strProblemDescription.replace("%c%", String.valueOf(countParsingExpectedParameters));
                        holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}

