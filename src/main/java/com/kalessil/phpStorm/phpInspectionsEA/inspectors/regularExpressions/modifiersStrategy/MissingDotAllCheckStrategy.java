package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MissingDotAllCheckStrategy {
    private static final String strProblemDescription = "/s modifier is probably missing (nested tags are not recognized).";

    final static private Pattern regexTagContentPattern;
    static {
        regexTagContentPattern = Pattern.compile(".*>\\.([*+])\\?<.*");
    }

    static public void apply(final String modifiers, final String pattern, @NotNull final PsiElement target, @NotNull final ProblemsHolder holder) {
        if (
            (StringUtils.isEmpty(modifiers) || modifiers.indexOf('s') == -1) &&
            !StringUtils.isEmpty(pattern) && pattern.indexOf('?') != -1
        ) {
            Matcher regexMatcher = regexTagContentPattern.matcher(pattern);
            if (regexMatcher.matches()) {
                holder.registerProblem(target, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
