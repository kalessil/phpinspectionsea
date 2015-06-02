package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage.FunctionCallCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage.PlainApiUseCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy.ShortClassDefinitionStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.AmbiguousAnythingTrimCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.NonGreedyTransformCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.SequentialClassesCollapseCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotOptimalRegularExpressionsInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "NotOptimalRegularExpressionsInspection";
    }

    private static HashSet<String> functions = null;
    private static HashSet<String> getFunctions() {
        if (null == functions) {
            functions = new HashSet<String>();

            functions.add("preg_filter");
            functions.add("preg_grep");
            functions.add("preg_match_all");
            functions.add("preg_match");
            functions.add("preg_replace_callback");
            functions.add("preg_replace");
            functions.add("preg_split");
            functions.add("preg_quote");
        }

        return functions;
    }

    static private Pattern regexWithModifiers = null;
    static private Pattern regexWithModifiersCurvy = null;
    static {
        regexWithModifiers = Pattern.compile("^([\\/#~])(.*)\\1([a-zA-Z]+)?$");
        regexWithModifiersCurvy = Pattern.compile("^\\{(.*)\\}([a-zA-Z]+)?$");
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !getFunctions().contains(strFunctionName)) {
                    return;
                }

                PsiElement[] params = reference.getParameters();
                if (params.length == 0 || !(params[0] instanceof StringLiteralExpression)) {
                    return;
                }

                String regex = ((StringLiteralExpression) params[0]).getContents();
                if (!StringUtil.isEmpty(regex)) {
                    Matcher regexMatcher = regexWithModifiers.matcher(regex);
                    if (regexMatcher.find()) {
                        String phpRegexPattern   = regexMatcher.group(2);
                        String phpRegexModifiers = regexMatcher.group(3);

                        checkCall(strFunctionName, reference, (StringLiteralExpression) params[0], phpRegexPattern, phpRegexModifiers);
                        return;
                    }

                    regexMatcher = regexWithModifiersCurvy.matcher(regex);
                    if (regexMatcher.find()) {
                        String phpRegexPattern   = regexMatcher.group(1);
                        String phpRegexModifiers = regexMatcher.group(2);

                        checkCall(strFunctionName, reference, (StringLiteralExpression) params[0], phpRegexPattern, phpRegexModifiers);
                        return;
                    }
                }
            }

            private void checkCall (String strFunctionName, FunctionReference reference, StringLiteralExpression target, String regex, String modifiers) {
                /** Modifiers validity (done):
                 * + /no-az-chars/i => /no-az-chars/
                 * + /no-dot-char/s => /no-dot-char/
                 * + /no-$/D => /no-$/
                 * + /no-^-or-$-occurrences/m => /no-^-or-$-occurrences/
                 * + /regexp/e => mark as deprecated, use preg_replace_callback instead
                 * + Check allowed PHP modifiers: eimsuxADJSUX
                 */
                DeprecatedModifiersCheckStrategy.apply(modifiers, target, holder);
                AllowedModifierCheckStrategy.apply(modifiers, target, holder);
                // UselessMultiLineModifierStrategy.apply(modifiers, regex, target, holder); -- we can not analyse if string has new lines
                UselessDollarEndOnlyModifierStrategy.apply(modifiers, regex, target, holder);
                UselessDotAllModifierCheckStrategy.apply(modifiers, regex, target, holder);
                UselessIgnoreCaseModifierCheckStrategy.apply(modifiers, regex, target, holder);

                /** Plain API simplification (done):
                 * + /^text/ => 0 === strpos(...) (match)
                 * + /text/ => false !== strpos(...) (match) / str_replace (replace)
                 * + /^text/i => 0 === stripos(...) (match)
                 * + /text/i => false !== stripos(...) (match) / str_ireplace (replace)
                 * + preg_quote => warning if second argument is not presented
                 * + preg_match_all without match argument preg_match
                 */
                FunctionCallCheckStrategy.apply(strFunctionName, reference, holder);
                PlainApiUseCheckStrategy.apply(strFunctionName, reference, modifiers, regex, holder);

                /** Classes shortening (done):
                 * + [0-9] => \d
                 * + [^0-9] => \D
                 * + [:digit:] => \d
                 * + [:word:] => \w
                 * + [^\w] => \W
                 * + [^\s] => \S
                 */
                ShortClassDefinitionStrategy.apply(regex, target, holder);

                /** Optimizations:
                 * (...) => (?:...) (if there is no back-reference)
                 *
                 * + .*?[symbol] at the end of regexp => [^symbol]*[symbol] (e.g. xml/html parsing using <.*?> vs <[^>]*>)
                 * + .+?[symbol] at the end of regexp => [^symbol]+[symbol]
                 * + / .* ··· /, / ···.* / => /···/ (remove leading and trailing .* without ^ or $, note: if no back-reference to \0)
                 * + [seq][seq]... => [seq]{N}
                 * + [seq][seq]+ => [seq]{2,}
                 * + [seq][seq]* => [seq]+
                 * + [seq][seq]? => [seq]{1,2}
                 */
                SequentialClassesCollapseCheckStrategy.apply(regex, target, holder);
                AmbiguousAnythingTrimCheckStrategy.apply(strFunctionName, reference, regex, target, holder);
                //NonGreedyTransformCheckStrategy.apply(regex, target, holder);
            }
        };
    }
}
