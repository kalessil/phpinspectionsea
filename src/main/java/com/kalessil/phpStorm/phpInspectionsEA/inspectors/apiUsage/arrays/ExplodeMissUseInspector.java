package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExplodeMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "Consider using '%s' instead (consumes less cpu and memory resources).";

    private static final Map<String, Integer> argumentMapping = new HashMap<>();
    static {
        argumentMapping.put("count", 0);    // -> "substr_count(%s%, %f%) + 1"
        argumentMapping.put("in_array", 1);  // -> "strpos(%s%, %f%) !== false"
        // "in_array" -> "strpos(%s%, %f%)" change behaviour
        // "current"  -> "strstr(%s%, %f%, true)": if fragment missing, strstr changes behaviour
    }

    @NotNull
    public String getShortName() {
        return "ExplodeMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String outerFunctionName = reference.getName();
                if (outerFunctionName != null && argumentMapping.containsKey(outerFunctionName)) {
                    final int targetArgumentPosition  = argumentMapping.get(outerFunctionName);
                    final PsiElement[] outerArguments = reference.getParameters();
                    if (outerArguments.length >= targetArgumentPosition + 1) {
                        final PsiElement targetArgument = outerArguments[targetArgumentPosition];
                        final Set<PsiElement> values    = PossibleValuesDiscoveryUtil.discover(targetArgument);
                        if (values.size() == 1) {
                            final PsiElement candidate = values.iterator().next();
                            if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                                final FunctionReference innerCall = (FunctionReference) candidate;
                                final String innerFunctionName    = innerCall.getName();
                                if (innerFunctionName != null && innerFunctionName.equals("explode")) {
                                    final PsiElement[] innerArguments = innerCall.getParameters();
                                    if (innerArguments.length == 2 && this.isExclusiveUse(targetArgument, reference)) {
                                        final String replacement;
                                        final PsiElement target;
                                        switch (outerFunctionName) {
                                            case "in_array":
                                                final PsiElement parent  = reference.getParent();
                                                final boolean isRegular  = ComparisonStyle.isRegular();
                                                String pattern           = isRegular ? "%sstrpos(%s, %s.%s.%s) !== false" : "false !== %sstrpos(%s, %s.%s.%s)";
                                                final boolean isInverted = parent instanceof UnaryExpression &&
                                                                           OpenapiTypesUtil.is(parent.getFirstChild(), PhpTokenTypes.opNOT);
                                                pattern                  = isInverted ? pattern.replace("!==", "===") : pattern;
                                                final boolean wrap       = (!isInverted && parent instanceof UnaryExpression) ||
                                                                           parent instanceof BinaryExpression;
                                                pattern                  = wrap ? '(' + pattern + ')' : pattern;
                                                replacement = String.format(
                                                        pattern,
                                                        reference.getImmediateNamespaceName(),
                                                        innerArguments[1].getText(),
                                                        innerArguments[0].getText(),
                                                        outerArguments[0].getText(),
                                                        innerArguments[0].getText()
                                                );
                                                target = isInverted ? parent : reference;
                                                break;
                                            case "count":
                                                replacement = String.format(
                                                        "%ssubstr_count(%s, %s) + 1",
                                                        reference.getImmediateNamespaceName(),
                                                        innerArguments[1].getText(),
                                                        innerArguments[0].getText()
                                                );
                                                target = reference;
                                                break;
                                            default:
                                                replacement = "...";
                                                target      = reference;
                                                break;
                                        }
                                        final String message = String.format(messagePattern, replacement);
                                        if (innerCall == targetArgument) {
                                            holder.registerProblem(target, message, new UseAlternativeFix(replacement));
                                        } else {
                                            holder.registerProblem(target, message);
                                        }
                                    }
                                }
                            }
                        }
                        values.clear();
                    }
                }
            }

            private boolean isExclusiveUse(@NotNull PsiElement candidate, @NotNull FunctionReference outer) {
                boolean result = candidate instanceof FunctionReference;
                if (!result) {
                    final Function scope      = ExpressionSemanticUtil.getScope(outer);
                    final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                    if (body != null) {
                        final long candidateUsages = PsiTreeUtil.findChildrenOfType(body, candidate.getClass()).stream()
                                .filter(expression -> OpenapiEquivalenceUtil.areEqual(expression, candidate))
                                .count();
                        result = candidateUsages == 2;
                    }
                }
                return result;
            }
        };
    }

    private static final class UseAlternativeFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the suggested 'explode(...)' alternative";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseAlternativeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
