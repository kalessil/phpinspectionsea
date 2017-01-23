package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MissingUnicodeModifierStrategy {
    private static final String message = "/u modifier is missing (unicode characters found).";

    final static private Pattern unicodeContentPattern;
    static {
        // Original regex: .*[^\u0000-\u007F]+.*
        unicodeContentPattern = Pattern.compile(".*[^\\u0000-\\u007F]+.*");
    }

    static public void apply(String modifiers, String pattern, @NotNull StringLiteralExpression target, @NotNull ProblemsHolder holder) {
        if (null == modifiers || modifiers.indexOf('u') != -1 || StringUtil.isEmpty(pattern)) {
            return;
        }

        if (unicodeContentPattern.matcher(pattern).matches()) {
            holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR);
        }
    }
}
