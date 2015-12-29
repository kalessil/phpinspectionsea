package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainApiUseCheckStrategy {
    private static final String strProblemStartTreatCase = "'0 === strpos(\"...\", \"%t%\")' can be used instead";
    private static final String strProblemStartIgnoreCase = "'0 === stripos(\"...\", \"%t%\")' can be used instead";
    private static final String strProblemContainsTreatCase = "'false !== strpos(\"...\", \"%t%\")' can be used instead";
    private static final String strProblemContainsIgnoreCase = "'false !== stripos(\"...\", \"%t%\")' can be used instead";
    private static final String strProblemReplaceTreatCase = "'str_replace(\"%t%\", ...)' can be used instead";
    private static final String strProblemReplaceIgnoreCase = "'str_ireplace(\"%t%\", ...)' can be used instead";
    private static final String strProblemCtypeCanBeUsed = "'%r%((string) %p%)' can be used instead";

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexTextSearch = null;
    static {
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+)$");
    }

    @SuppressWarnings("CanBeFinal")
    static private HashMap<String, String> ctypePatterns = null;
    static {
        ctypePatterns = new HashMap<String, String>();

        ctypePatterns.put("^\\d+$",          "ctype_digit");
        ctypePatterns.put("^[^\\d]+$",       "!ctype_digit");

        ctypePatterns.put("^[A-Za-z]+$",     "ctype_alpha");
        ctypePatterns.put("^[^A-Za-z]+$",    "!ctype_alpha");

        ctypePatterns.put("^[A-Za-z0-9]+$",  "ctype_alnum");
        ctypePatterns.put("^[^A-Za-z0-9]+$", "!ctype_alnum");
    }

    static public void apply(
            final String functionName, @NotNull final FunctionReference reference,
            final String modifiers, final String pattern,
            @NotNull final ProblemsHolder holder
    ) {
        final int parametersCount = reference.getParameters().length;
        if (parametersCount >= 2 && !StringUtil.isEmpty(pattern)) {
            String patternAdapted = pattern
                    .replace("a-zA-Z",    "A-Za-z")
                    .replace("0-9A-Za-z", "A-Za-z0-9");

            Matcher regexMatcher = regexTextSearch.matcher(patternAdapted);
            if (regexMatcher.find()) {
                final boolean ignoreCase = !StringUtil.isEmpty(modifiers) && modifiers.indexOf('i') >= 0;
                final boolean startWith = !StringUtil.isEmpty(regexMatcher.group(1));

                /* analyse if pattern is the one strategy targeting */
                String strProblemDescription = null;
                if (functionName.equals("preg_match") && startWith) {
                    strProblemDescription = ignoreCase ? strProblemStartIgnoreCase : strProblemStartTreatCase;
                }
                if (functionName.equals("preg_match") && !startWith) {
                    strProblemDescription = ignoreCase ? strProblemContainsIgnoreCase : strProblemContainsTreatCase;
                }
                if (functionName.equals("preg_replace") && !startWith) {
                    strProblemDescription = ignoreCase ? strProblemReplaceIgnoreCase : strProblemReplaceTreatCase;
                }

                if (null != strProblemDescription) {
                    String strError = strProblemDescription.replace("%t%", regexMatcher.group(2));
                    holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            /* investigate using ctype_* functions instead */
            if (2 == parametersCount && functionName.equals("preg_match") && ctypePatterns.containsKey(patternAdapted)) {
                String strError = strProblemCtypeCanBeUsed
                        .replace("%r%", ctypePatterns.get(patternAdapted))
                        .replace("%p%", reference.getParameters()[1].getText());
                holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
    }
}
