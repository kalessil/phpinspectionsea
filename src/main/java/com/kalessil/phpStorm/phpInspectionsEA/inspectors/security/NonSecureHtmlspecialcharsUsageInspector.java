package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureHtmlspecialcharsUsageInspector extends BasePhpInspection {
    private static final String message = "Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.";

    private static final Set<String> flags = new HashSet<>();
    static {
        flags.add("ENT_QUOTES");
        flags.add("ENT_COMPAT");
    }

    @NotNull
    public String getShortName() {
        return "NonSecureHtmlspecialcharsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("htmlspecialchars")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0 && !this.isTestContext(reference)) {
                        final PsiElement usedFlags = arguments.length > 1 ? arguments[1] : null;
                        String updatedFlags        = null;
                        if (usedFlags == null) {
                            updatedFlags = "ENT_QUOTES | ENT_HTML5";
                        } else if (usedFlags instanceof ConstantReference) {
                            if (!flags.contains(((ConstantReference) usedFlags).getName())) {
                                updatedFlags = String.format("ENT_QUOTES | %s", usedFlags.getText());
                            }
                        } else if (usedFlags instanceof BinaryExpression) {
                            if (((BinaryExpression) usedFlags).getOperationType() == PhpTokenTypes.opBIT_OR) {
                                final boolean usesQuoting = PsiTreeUtil.findChildrenOfType(usedFlags, ConstantReference.class)
                                        .stream().anyMatch(constant -> flags.contains(constant.getName()));
                                if (!usesQuoting) {
                                    updatedFlags = String.format("ENT_QUOTES | %s", usedFlags.getText());
                                }
                            }
                        }
                        if (updatedFlags != null) {
                            final String[] updatedArguments = new String[Math.max(2, arguments.length)];
                            Arrays.stream(arguments).map(PsiElement::getText).collect(Collectors.toList()).toArray(updatedArguments);
                            updatedArguments[1]             = updatedFlags;
                            final String replacement        = String.format("htmlspecialchars(%s)", String.join(", ", updatedArguments));
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new EscapeAllQuotesFix(replacement));
                        }
                    }
                }
                /* TODO: call_user_func, array_walk - dynamic contexts */
            }
        };
    }

    private static final class EscapeAllQuotesFix extends UseSuggestedReplacementFixer {
        private static final String title = "Escape single and double quotes";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        EscapeAllQuotesFix (@NotNull String expression) {
            super(expression);
        }
    }
}