package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage.FunctionCallCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage.PlainApiUseCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy.ShortClassDefinitionStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.classesStrategy.SuspiciousCharactersRangeSpecificationStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy.GreedyCharactersSetCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy.NotMutuallyExclusiveContiguousQuantifiedTokensStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.explosiveStrategy.QuantifierCompoundsQuantifierCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.modifiersStrategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.AmbiguousAnythingTrimCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.SequentialClassesCollapseCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.SingleCharactersAlternationStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy.UnnecessaryCaseManipulationCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class NotOptimalRegularExpressionsInspector extends PhpInspection {
    private static final String messageNoDelimiters = "The regular expression delimiters are missing (it should be e.g. '/<regex-here>/').";

    @NotNull
    @Override
    public String getShortName() {
        return "NotOptimalRegularExpressionsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Non-optimal regular expression";
    }

    private static final Set<String> functions = new HashSet<>();
    static {
        functions.add("preg_filter");
        functions.add("preg_grep");
        functions.add("preg_match_all");
        functions.add("preg_match");
        functions.add("preg_replace_callback");
        functions.add("preg_replace");
        functions.add("preg_split");
        functions.add("preg_quote");
    }

    final static private List<Pattern> matchers = new ArrayList<>();
    static {
        /* same regexes in BypassedUrlValidationInspector (in order to not couple inspections) */
        matchers.add(Pattern.compile("^([^{<(\\[])(.*)(\\1)([a-zA-Z]+)?$", Pattern.DOTALL));
        matchers.add(Pattern.compile("^(\\{)(.*)(\\})([a-zA-Z]+)?$", Pattern.DOTALL));
        matchers.add(Pattern.compile("^(<)(.*)(>)([a-zA-Z]+)?$", Pattern.DOTALL));
        matchers.add(Pattern.compile("^(\\()(.*)(\\))([a-zA-Z]+)?$", Pattern.DOTALL));
        matchers.add(Pattern.compile("^(\\[)(.*)(\\])([a-zA-Z]+)?$", Pattern.DOTALL));
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        final boolean checkCall    = !(arguments[0] instanceof ArrayCreationExpression);
                        final Set<String> patterns = this.extractPatterns(arguments[0]);
                        for (final String pattern : patterns) {
                            if (pattern != null && !pattern.isEmpty()) {
                                boolean hasDelimiters = false;
                                for (final Pattern regex : matchers) {
                                    final Matcher matcher = regex.matcher(pattern);
                                    if (hasDelimiters = matcher.find()) {
                                        final String phpRegexPattern   = matcher.group(2);
                                        final String phpRegexModifiers = matcher.group(4);
                                        this.checkRegex(functionName, reference, arguments[0], phpRegexPattern, phpRegexModifiers);
                                        if (checkCall) {
                                            this.checkCall(functionName, reference, phpRegexPattern, phpRegexModifiers);
                                        }
                                        break;
                                    }
                                }
                                if (!hasDelimiters && !functionName.equals("preg_quote")) {
                                    holder.registerProblem(arguments[0], messageNoDelimiters);
                                }
                            }
                        }
                        patterns.clear();
                    }
                }
            }

            private Set<String> extractPatterns(@NotNull PsiElement candidate) {
                final Set<String> result = new HashSet<>();
                if (candidate instanceof ArrayCreationExpression) {
                    for (final PsiElement child : candidate.getChildren()) {
                        if (child instanceof ArrayHashElement) {
                            result.addAll(ExpressionSemanticUtil.resolveAsString(((ArrayHashElement) child).getValue()));
                        } else if (child instanceof PhpPsiElement) {
                            result.addAll(ExpressionSemanticUtil.resolveAsString(child));
                        }
                    }
                } else {
                    result.addAll(ExpressionSemanticUtil.resolveAsString(candidate));
                }
                return result;
            }

            private void checkRegex (
                    @NotNull String functionName,
                    @NotNull FunctionReference reference,
                    @NotNull PsiElement target,
                    String regex,
                    String modifiers
            ) {
                /* Modifiers validity (done):
                 * + /no-az-chars/i => /no-az-chars/
                 * + /no-dot-char/s => /no-dot-char/
                 * + /no-$/D => /no-$/
                 * + /no-^-or-$-occurrences/m => /no-^-or-$-occurrences/
                 * + /regexp/e => mark as deprecated, use preg_replace_callback instead
                 * + Check allowed PHP modifiers: eimsuxADJSUX
                 */
                DeprecatedModifiersCheckStrategy.apply(modifiers, target, holder);
                AllowedModifierCheckStrategy.apply(functionName, modifiers, target, holder);
                UselessDollarEndOnlyModifierStrategy.apply(modifiers, regex, target, holder);
                UselessDotAllModifierCheckStrategy.apply(modifiers, regex, target, holder);
                UselessIgnoreCaseModifierCheckStrategy.apply(modifiers, regex, target, holder);
                UselessUngreedyModifierCheckStrategy.apply(modifiers, regex, target, holder);

                /* Classes shortening (done):
                 * + [0-9] => \d
                 * + [^0-9] => \D
                 * + [:digit:] => \d
                 * + [:word:] => \w
                 * + [^\w] => \W
                 * + [^\s] => \S
                 */
                ShortClassDefinitionStrategy.apply(modifiers, regex, target, holder);

                /* Optimizations:
                 * (...) => (?:...) (if there is no back-reference)
                 *
                 * + .*?[symbol] at the end of regexp => [^symbol]*[symbol] (e.g. xml/html parsing using <.*?> vs <[^>]*>)
                 * + .+?[symbol] at the end of regexp => [^symbol]+[symbol]
                 * + / .* ··· /, / ···.* / => /···/ (remove leading and trailing .* without ^ or $, note: if no back-reference to \0)
                 * + [seq][seq]... => [seq]{N}
                 * + [seq][seq]+ => [seq]{2,}
                 * + [seq][seq]* => [seq]+
                 * + [seq][seq]? => [seq]{1,2}
                 *
                 * + greedy character classes [\d\w][\D\W]
                 * + dangerous (a+)+ pattern
                 */
                SequentialClassesCollapseCheckStrategy.apply(regex, target, holder);
                AmbiguousAnythingTrimCheckStrategy.apply(functionName, reference, regex, target, holder);
                GreedyCharactersSetCheckStrategy.apply(regex, target, holder);

                boolean greedy = QuantifierCompoundsQuantifierCheckStrategy.apply(regex, target, holder);
                greedy         = NotMutuallyExclusiveContiguousQuantifiedTokensStrategy.apply(regex, target, holder)|| greedy;
                if (!greedy) {
                    SingleCharactersAlternationStrategy.apply(regex, target, holder);
                }

                /* probably bugs: nested tags check without /s, unicode characters without /u */
                boolean missingModifiers = MissingDotAllCheckStrategy.apply(modifiers, regex, target, holder);
                missingModifiers         = MissingUnicodeModifierStrategy.apply(functionName, modifiers, regex, target, holder) || missingModifiers;
                if (!missingModifiers) {
                    /* suspicious characters specification:  [...A-z...]/[...a-Z...] */
                    SuspiciousCharactersRangeSpecificationStrategy.apply(modifiers, regex, target, holder);
                }
            }

            private void checkCall (
                    @NotNull String functionName,
                    @NotNull FunctionReference reference,
                    String regex,
                    String modifiers
            ) {
                /* Plain API simplification (done):
                 * + /^text/ => 0 === strpos(...) (match)
                 * + /text/ => false !== strpos(...) (match) / str_replace (replace)
                 * + /^text/i => 0 === stripos(...) (match)
                 * + /text/i => false !== stripos(...) (match) / str_ireplace (replace)
                 * + preg_quote => warning if second argument is not presented
                 * + preg_match_all without match argument preg_match
                 */
                FunctionCallCheckStrategy.apply(functionName, reference, holder);
                PlainApiUseCheckStrategy.apply(functionName, reference, modifiers, regex, holder);

                /* source checks */
                UnnecessaryCaseManipulationCheckStrategy.apply(functionName, reference, modifiers, holder);
            }
        };
    }
}
