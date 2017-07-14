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
    private static final String patternStringIdentical   = "'%e%' can be used instead.";
    private static final String patternStartsWith        = "'%e%' can be used instead.";
    private static final String patternContains          = "'%e%' can be used instead.";
    private static final String patternStringReplace     = "'%e%' can be used instead.";
    private static final String patternExplodeCanBeUsed  = "'explode(\"...\", %s%%l%)' can be used instead.";
    private static final String patternTrim              = "'%f%(%s%, \"...\")' can be used instead.";

    final static private Pattern regexTextSearch;
    static {
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+)(\\$?)$");
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
        // 	^((\^([^\.])[\+\*])|(([^\.])[\+\*]\$)|(\^([^\.])[\+\*]\|\7[\+\*]\$))$
        trimPatterns = Pattern.compile("^((\\^([^\\.])[\\+\\*])|(([^\\.])[\\+\\*]\\$)|(\\^([^\\.])[\\+\\*]\\|\\7[\\+\\*]\\$))$");
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
                final boolean endsWith   = !StringUtil.isEmpty(regexMatcher.group(3));

                /* analyse if pattern is the one strategy targeting */
                String messagePattern = null;
                LocalQuickFix fixer   = null;

                if (parametersCount == 2 && functionName.equals("preg_match")) {
                    if (startWith && endsWith && !ignoreCase) {
                        final String replacement = "\"%p%\" === %s%"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText());
                        messagePattern = patternStringIdentical.replace("%e%", replacement);
                        fixer          = new UseStringComparisonFix(replacement);
                    } else if (startWith && !endsWith) {
                        // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                        final String replacement = "0 === %f%(%s%, \"%p%\")"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText())
                            .replace("%f%", ignoreCase ? "stripos" : "strpos");
                        messagePattern = patternStartsWith.replace("%e%", replacement);
                        fixer          = new UseStringPositionFix(replacement);
                    } else if (!startWith && !endsWith) {
                        // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                        final String replacement = "false !== %f%(%s%, \"%p%\")"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText())
                            .replace("%f%", ignoreCase ? "stripos" : "strpos");
                        messagePattern = patternContains.replace("%e%", replacement);
                        fixer          = new UseStringPositionFix(replacement);
                    }
                } else if (parametersCount == 3 && functionName.equals("preg_replace") && !startWith && !endsWith) {
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
                    return;
                }
            }

            /* investigate using *trim(...) instead */
            if (
                parametersCount == 3 && functionName.equals("preg_replace") &&
                params[1] instanceof StringLiteralExpression && params[1].getText().length() == 2 &&
                trimPatterns.matcher(pattern).find()
            ) {
                // mixed preg_replace ( mixed $pattern , mixed $replacement , mixed $subject [, int $limit = -1 [, int &$count ]] )
                String function = "trim";
                if (!pattern.startsWith("^")) {
                    function = "rtrim";
                } else if (!pattern.endsWith("$")) {
                    function = "ltrim";
                }
                // group(7) is the char to trim

                final String message = patternTrim
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
                        .replace("%l%", parametersCount > 2 ? ", " + params[2].getText() : "")
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

    private static class UseStringComparisonFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use string comparison instead";
        }

        UseStringComparisonFix(@NotNull String expression) {
            super(expression);
        }
    }
}
