package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainApiUseCheckStrategy {
    private static final String strProblemStartTreatCase = "'0 === strpos(\"%t%\", \"...\")' can be used instead";
    private static final String strProblemStartIgnoreCase = "'0 === stripos(\"%t%\", \"...\")' can be used instead";
    private static final String strProblemContainsTreatCase = "'false !== strpos(\"%t%\", \"...\")' can be used instead";
    private static final String strProblemContainsIgnoreCase = "'false !== stripos(\"%t%\", \"...\")' can be used instead";
    private static final String strProblemReplaceTreatCase = "'str_replace(\"%t%\", \"...\")' can be used instead";
    private static final String strProblemReplaceIgnoreCase = "'str_ireplace(\"%t%\", \"...\")' can be used instead";

    static private Pattern regexTextSearch = null;
    static {
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+)$");
    }

    static public void apply(
            final String functionName, @NotNull final FunctionReference reference,
            final String modifiers, final String pattern,
            @NotNull final ProblemsHolder holder
    ) {
        if (reference.getParameters().length >= 2 && !StringUtil.isEmpty(pattern)) {
            Matcher regexMatcher = regexTextSearch.matcher(pattern);
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
        }
    }
}
