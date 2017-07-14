package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import org.jetbrains.annotations.NotNull;

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

final public class PlainApiUseCheckStrategy {
    private static final String patternStartsWith        = "'%e%' can be used instead.";
    private static final String patternContains          = "'%e%' can be used instead.";
    private static final String patternStringReplace     = "'%e%' can be used instead.";
    private static final String patternExplodeCanBeUsed  = "'explode(\"...\", %s%%l%)' can be used instead.";
    private static final String strProblemTrimsCanBeUsed = "'%f%(%s%, \"...\")' can be used instead.";

    final static private Pattern regexTextSearch;
    static {
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+)$");
    }

    final static private Pattern regexHasRegexAttributes;
    static {
        // 	([^\\][\^\$\.\*\+\?\\\[\]\(\)\{\}\!\|\-])|([^\\]?\\[dDhHsSvVwWR])
        regexHasRegexAttributes = Pattern.compile("([^\\\\][\\^\\$\\.\\*\\+\\?\\\\\\[\\]\\(\\)\\{\\}\\!\\|\\-])|([^\\\\]?\\\\[dDhHsSvVwWR])");
    }

    final static private Pattern regexSingleCharSet;
    static {
        // 	^(\[[^\.]{1}\]|[^\.]{1})$
        regexSingleCharSet = Pattern.compile("^(\\[[^\\.]{1}\\]|[^\\.]{1})$");
    }

    final static private Pattern trimPatterns;
    static {
        // 	^((\^[^\.][\+\*])|([^\.][\+\*]\$)|(\^[^\.][\+\*]\|[^\.][\+\*]\$))$
        trimPatterns = Pattern.compile("^((\\^[^\\.][\\+\\*])|([^\\.][\\+\\*]\\$)|(\\^[^\\.][\\+\\*]\\|[^\\.][\\+\\*]\\$))$");
    }

    static public void apply(
            final String functionName, @NotNull final FunctionReference reference,
            final String modifiers, final String pattern,
            @NotNull final ProblemsHolder holder
    ) {
        final PsiElement[] params = reference.getParameters();
        final int parametersCount = params.length;
        if (parametersCount >= 2 && !StringUtil.isEmpty(pattern)) {
            final String patternAdapted = pattern
                    .replace("a-zA-Z",    "A-Za-z")
                    .replace("0-9A-Za-z", "A-Za-z0-9");

            final Matcher regexMatcher = regexTextSearch.matcher(patternAdapted);
            if (regexMatcher.find()) {
                final boolean ignoreCase = !StringUtil.isEmpty(modifiers) && modifiers.indexOf('i') != -1;
                final boolean startWith  = !StringUtil.isEmpty(regexMatcher.group(1));

                /* analyse if pattern is the one strategy targeting */
                String messagePattern = null;
                LocalQuickFix fixer   = null;
                // TODO: preg_match('/^whatever$/',  $string) -> "whatever" === $string
                if (functionName.equals("preg_match") && startWith && params.length == 2) {
                    // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                    final String replacement = "0 === %f%(%s%, \"%p%\")"
                        .replace("%p%", regexMatcher.group(2))
                        .replace("%s%", params[1].getText())
                        .replace("%f%", ignoreCase ? "stripos" : "strpos");
                    messagePattern = patternStartsWith.replace("%e%", replacement);
                    fixer          = new UseStringPositionFix(replacement);
                } else if (functionName.equals("preg_match") && !startWith && params.length == 2) {
                    // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                    final String replacement = "false !== %f%(%s%, \"%p%\")"
                        .replace("%p%", regexMatcher.group(2))
                        .replace("%s%", params[1].getText())
                        .replace("%f%", ignoreCase ? "stripos" : "strpos");
                    messagePattern = patternContains.replace("%e%", replacement);
                    fixer          = new UseStringPositionFix(replacement);
                } else if (functionName.equals("preg_replace") && !startWith && params.length == 3) {
                    // mixed str_replace ( mixed $search , mixed $replace , mixed $subject [, int &$count ] )
                    final String replacement = "%f%(\"%p%\", %r%, %s%)"
                        .replace("%s%", params[2].getText())
                        .replace("%r%", params[1].getText())
                        .replace("%p%", regexMatcher.group(2))
                        .replace("%f%", ignoreCase ? "str_ireplace" : "str_replace");
                    messagePattern = patternStringReplace.replace("%e%", replacement);
                    fixer          = new UseStringReplaceFix(replacement);
                }

                if (messagePattern != null) {
                    final String message = messagePattern.replace("%t%", regexMatcher.group(2));
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixer);
                }
            }

            /* investigate using *trim(...) instead */
            if (
                3 == parametersCount && functionName.equals("preg_replace") && params[1] instanceof StringLiteralExpression &&
                ((StringLiteralExpression) params[1]).getContents().length() == 0 && trimPatterns.matcher(patternAdapted).find()
            ) {
                String function = "trim";
                if (!pattern.startsWith("^")) {
                    function = "rtrim";
                }
                if (!pattern.endsWith("$")) {
                    function = "ltrim";
                }

                final String message = strProblemTrimsCanBeUsed
                        .replace("%f%", function)
                        .replace("%s%", params[2].getText());
                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }

            /* investigate using explode(...) instead */
            if (
                (parametersCount == 2 || parametersCount == 3) && functionName.equals("preg_split") && StringUtil.isEmpty(modifiers) &&
                (regexSingleCharSet.matcher(patternAdapted).find() || !regexHasRegexAttributes.matcher(patternAdapted).find())
            ) {
                final String message = patternExplodeCanBeUsed
                        .replace("%l%", params.length > 2 ? ", " + params[2].getText() : "")
                        .replace("%s%", params[1].getText());
                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
    }

    private static class UseStringReplaceFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use plain string replacement instead";
        }

        UseStringReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static class UseStringPositionFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use plain string search instead";
        }

        UseStringPositionFix(@NotNull String expression) {
            super(expression);
        }
    }
}
