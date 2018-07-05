package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private static final String messagePattern = "'%e%' can be used instead.";

    final static private Pattern regexTextSearch;
    static {
        // ^(\^?)([\w-]+|\\[.*+?])(\$?)$
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+|\\\\[.*+?])(\\$?)$");
    }

    final static private Pattern regexHasRegexAttributes;
    static {
        // 	([^\\][\^\$\.\*\+\?\\\[\]\(\)\{\}\!\|\-])|([^\\]?\\[dDhHsSvVwWR])
        regexHasRegexAttributes = Pattern.compile("([^\\\\][\\^\\$\\.\\*\\+\\?\\\\\\[\\]\\(\\)\\{\\}\\!\\|\\-])|([^\\\\]?\\\\[dDhHsSvVwWR])");
    }

    final static private Pattern regexSingleCharSet;
    static {
        // 	^(\[[^\.]\]|[^\.])$
        regexSingleCharSet = Pattern.compile("^(\\[[^\\.]\\]|[^\\.])$");
    }

    final static private Pattern trimPatterns;
    static {
        // ^((\^([^\.]|\\s)[\+\*])|(([^\.]|\\s)[\+\*]\$)|(\^([^\.]|\\s)[\+\*]\|\7[\+\*]\$))$
        trimPatterns = Pattern.compile("^((\\^([^\\.]|\\\\s)[\\+\\*])|(([^\\.]|\\\\s)[\\+\\*]\\$)|(\\^([^\\.]|\\\\s)[\\+\\*]\\|\\7[\\+\\*]\\$))$");
    }

    static public void apply(
        final String functionName,
        @NotNull final FunctionReference reference,
        @Nullable final String modifiers,
        final String pattern,
        @NotNull final ProblemsHolder holder
    ) {
        final PsiElement[] params = reference.getParameters();
        final int parametersCount = params.length;
        if (parametersCount >= 2 && !StringUtils.isEmpty(pattern)) {
            final String patternAdapted = pattern
                    .replace("a-zA-Z",    "A-Za-z")
                    .replace("0-9A-Za-z", "A-Za-z0-9");

            final Matcher regexMatcher = regexTextSearch.matcher(patternAdapted);
            if (regexMatcher.find()) {
                final boolean ignoreCase = !StringUtils.isEmpty(modifiers) && modifiers.indexOf('i') != -1;
                final boolean startWith  = !StringUtils.isEmpty(regexMatcher.group(1));
                final boolean endsWith   = !StringUtils.isEmpty(regexMatcher.group(3));

                /* analyse if pattern is the one strategy targeting */
                String message      = null;
                LocalQuickFix fixer = null;

                if (parametersCount == 2 && functionName.equals("preg_match")) {
                    if (startWith && endsWith && !ignoreCase) {
                        final String replacement = "\"%p%\" === %s%"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText());
                        message = messagePattern.replace("%e%", replacement);
                        fixer   = new UseStringComparisonFix(replacement);
                    } else if (startWith && !endsWith) {
                        // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                        final String replacement = "0 === %f%(%s%, \"%p%\")"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText())
                            .replace("%f%", ignoreCase ? "stripos" : "strpos");
                        message = messagePattern.replace("%e%", replacement);
                        fixer   = new UseStringPositionFix(replacement);
                    } else if (!startWith && !endsWith) {
                        // mixed strpos ( string $haystack , mixed $needle [, int $offset = 0 ] )
                        final String replacement = "false !== %f%(%s%, \"%p%\")"
                            .replace("%p%", regexMatcher.group(2))
                            .replace("%s%", params[1].getText())
                            .replace("%f%", ignoreCase ? "stripos" : "strpos");
                        message = messagePattern.replace("%e%", replacement);
                        fixer   = new UseStringPositionFix(replacement);
                    }
                } else if (parametersCount == 3 && functionName.equals("preg_replace") && !startWith && !endsWith) {
                    // mixed str_replace ( mixed $search , mixed $replace , mixed $subject [, int &$count ] )
                    final String replacement = "%f%(\"%p%\", %r%, %s%)"
                        .replace("%s%", params[2].getText())
                        .replace("%r%", params[1].getText())
                        .replace("%p%", regexMatcher.group(2))
                        .replace("%f%", ignoreCase ? "str_ireplace" : "str_replace");
                    message = messagePattern.replace("%e%", replacement);
                    fixer   = new UseStringReplaceFix(replacement);
                }

                if (message != null) {
                    holder.registerProblem(reference, message, fixer);
                    return;
                }
            }

            /* investigate using *trim(...) instead */
            final Matcher trimMatcher = trimPatterns.matcher(patternAdapted);
            if (
                parametersCount == 3 && functionName.equals("preg_replace") &&
                params[1] instanceof StringLiteralExpression && params[1].getText().length() == 2 &&
                trimMatcher.find()
            ) {
                /* false-positives: the `m` modifier make the replacement impossible */
                if (modifiers != null && modifiers.indexOf('m') != -1) {
                    return;
                }

                // mixed preg_replace ( mixed $pattern , mixed $replacement , mixed $subject [, int $limit = -1 [, int &$count ]] )
                String function = "trim";
                if (!pattern.startsWith("^")) {
                    function = "rtrim";
                } else if (!pattern.endsWith("$")) {
                    function = "ltrim";
                }

                String characterToTrim = trimMatcher.group(7);
                characterToTrim        = characterToTrim == null ? trimMatcher.group(5) : characterToTrim;
                characterToTrim        = characterToTrim == null ? trimMatcher.group(3) : characterToTrim;
                final String replacement = "%f%(%s%, '%p%')"
                    .replace(", '%p%'", characterToTrim.equals("\\s") ? "" : ", '%p%'")
                    .replace("%p%", characterToTrim)
                    .replace("%s%", params[2].getText())
                    .replace("%f%", function);
                holder.registerProblem(reference, messagePattern.replace("%e%", replacement), new UseTrimFix(replacement));
                return;
            }

            /* investigate using explode(...) instead */
            if (
                (parametersCount == 2 || parametersCount == 3) && functionName.equals("preg_split") && StringUtils.isEmpty(modifiers) &&
                (regexSingleCharSet.matcher(patternAdapted).find() || !regexHasRegexAttributes.matcher(patternAdapted).find())
            ) {
                final String replacement = "explode(\"%p%\", %s%%l%)"
                    .replace("%l%", parametersCount > 2 ? ", " + params[2].getText() : "")
                    .replace("%s%", params[1].getText())
                    .replace("%p%", patternAdapted);
                holder.registerProblem(reference, messagePattern.replace("%e%", replacement), new UseExplodeFix(replacement));
            }
        }
    }

    private static final class UseStringReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use plain string replacement instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStringReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseStringPositionFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use plain string search instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStringPositionFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseStringComparisonFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use string comparison instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStringComparisonFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseTrimFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use trim instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseTrimFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseExplodeFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use explode instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseExplodeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
