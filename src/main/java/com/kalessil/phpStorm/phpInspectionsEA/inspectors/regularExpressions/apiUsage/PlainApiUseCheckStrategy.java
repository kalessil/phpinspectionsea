package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang3.StringUtils;
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
    private static final String messagePattern = "'%s' can be used instead.";

    final static private Pattern regexTextSearch;
    static {
        // ^(\^?)([\w-]+|\\[.*+?])(\$?)$
        regexTextSearch = Pattern.compile("^(\\^?)([\\w-]+|\\\\[.*+?])(\\$?)$");
    }

    final static private Pattern regexHasRegexAttributes;
    static {
        // 	([^\\][\^\$\.\*\+\?\\\[\]\(\)\{\}\!\|\-])|([^\\]?\\[dDhHsSvVwWRb])
        regexHasRegexAttributes = Pattern.compile("([^\\\\][\\^\\$\\.\\*\\+\\?\\\\\\[\\]\\(\\)\\{\\}\\!\\|\\-])|([^\\\\]?\\\\[dDhHsSvVwWRb])");
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
                    final boolean isInverted = isPregMatchInverted(reference);

                    if (startWith && endsWith && !ignoreCase) {
                        final String replacement = String.format(
                                "\"%s\" %s %s",
                                unescape(regexMatcher.group(2)),
                                isInverted ? "!==" : "===",
                                params[1].getText()
                        );
                        message = String.format(messagePattern, replacement);
                        fixer   = new UseStringComparisonFix(replacement);
                    } else if (startWith && !endsWith) {
                        final String replacement = String.format(
                                "0 %s %s(%s, \"%s\")",
                                isInverted ? "!==" : "===",
                                ignoreCase ? "stripos" : "strpos",
                                params[1].getText(),
                                unescape(regexMatcher.group(2))
                        );
                        message = String.format(messagePattern, replacement);
                        fixer   = new UseStringPositionFix(replacement);
                    } else if (!startWith && !endsWith) {
                        final String replacement = String.format(
                                "false %s %s(%s, \"%s\")",
                                isInverted ? "===" : "!==",
                                ignoreCase ? "stripos" : "strpos",
                                params[1].getText(),
                                unescape(regexMatcher.group(2))
                        );
                        message = String.format(messagePattern, replacement);
                        fixer   = new UseStringPositionFix(replacement);
                    }
                } else if (parametersCount == 3 && functionName.equals("preg_replace") && !startWith && !endsWith) {
                    // mixed str_replace ( mixed $search , mixed $replace , mixed $subject [, int &$count ] )
                    final String replacement = "%f%(\"%p%\", %r%, %s%)"
                        .replace("%s%", params[2].getText())
                        .replace("%r%", params[1].getText())
                        .replace("%p%", unescape(regexMatcher.group(2)))
                        .replace("%f%", ignoreCase ? "str_ireplace" : "str_replace");
                    message = String.format(messagePattern, replacement);
                    fixer   = new UseStringReplaceFix(replacement);
                }

                if (message != null) {
                    holder.registerProblem(
                            getPregMatchContext(reference),
                            MessagesPresentationUtil.prefixWithEa(message),
                            fixer
                    );
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
                /* false-positives: the `m` or `u` modifiers making the replacement impossible */
                if (modifiers != null && (modifiers.indexOf('m') != -1 || modifiers.indexOf('u') != -1)) {
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
                    .replace("%p%", unescape(characterToTrim))
                    .replace("%s%", params[2].getText())
                    .replace("%f%", function);
                holder.registerProblem(
                        reference,
                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                        new UseTrimFix(replacement)
                );
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
                    .replace("%p%", unescape(patternAdapted));
                holder.registerProblem(
                        reference,
                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                        new UseExplodeFix(replacement)
                );
            }
        }
    }

    private static boolean isPregMatchInverted(@NotNull FunctionReference reference) {
        boolean result          = false;
        final PsiElement parent = reference.getParent();
        if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
            if (parent instanceof UnaryExpression) {
                result = OpenapiTypesUtil.is(((UnaryExpression) parent).getOperation(), PhpTokenTypes.opNOT);
            }
        } else if (parent instanceof BinaryExpression) {
            // inverted: < 1, == 0, === 0, != 1, !== 1
            // not inverted: > 0, == 1, === 1, != 0, !== 0
            final BinaryExpression binary = (BinaryExpression) parent;
            final IElementType operator   = binary.getOperationType();
            if (operator == PhpTokenTypes.opLESS || OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                if (OpenapiTypesUtil.isNumber(second)) {
                    final String number = second.getText();
                    result              = operator == PhpTokenTypes.opLESS && number.equals("1") ||
                                          operator == PhpTokenTypes.opEQUAL && number.equals("0") ||
                                          operator == PhpTokenTypes.opIDENTICAL && number.equals("0") ||
                                          operator == PhpTokenTypes.opNOT_EQUAL && number.equals("1") ||
                                          operator == PhpTokenTypes.opNOT_IDENTICAL && number.equals("1");
                }
            }
        }
        return result;
    }

    private static PsiElement getPregMatchContext(@NotNull FunctionReference reference) {
        PsiElement result       = reference;
        final PsiElement parent = reference.getParent();
        if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
            if (parent instanceof UnaryExpression) {
                final UnaryExpression unary = (UnaryExpression) parent;
                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                    result = parent;
                }
            }
        } else if (parent instanceof BinaryExpression) {
            final BinaryExpression binary  = (BinaryExpression) parent;
            final IElementType operator    = binary.getOperationType();
            final boolean isTargetOperator = OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator) ||
                                             PhpTokenTypes.tsCOMPARE_ORDER_OPS.contains(operator);
            if (isTargetOperator) {
                final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                if (OpenapiTypesUtil.isNumber(second)) {
                    result = parent;
                }
            }
        }
        return result;
    }

    private static String unescape(@NotNull String string) {
        return string.replaceAll("\\\\([.+*?\\-])", "$1");
    }

    private static final class UseStringReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use plain string replacement instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
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
            return MessagesPresentationUtil.prefixWithEa(title);
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
            return MessagesPresentationUtil.prefixWithEa(title);
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
            return MessagesPresentationUtil.prefixWithEa(title);
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseExplodeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
