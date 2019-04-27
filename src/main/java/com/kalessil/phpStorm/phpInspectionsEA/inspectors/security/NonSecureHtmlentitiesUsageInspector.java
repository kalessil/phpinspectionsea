package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class NonSecureHtmlentitiesUsageInspector extends PhpInspection {
    private static final String messageHarden   = "Single quotes handling is not specified, please use ENT_QUOTES or ENT_COMPAT as second argument.";
    private static final String messageCallback = "Single quotes are not handled, please make use of ENT_QUOTES or ENT_COMPAT flags.";

    private static final Set<String> flags     = new HashSet<>();
    private static final Set<String> functions = new HashSet<>();
    static {
        flags.add("ENT_QUOTES");
        flags.add("ENT_COMPAT");
        flags.add("ENT_NOQUOTES");

        functions.add("call_user_func");
        functions.add("array_map");
    }

    @NotNull
    public String getShortName() {
        return "NonSecureHtmlentitiesUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    if (functionName.equals("htmlentities")) {
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
                                final String replacement        = String.format("htmlentities(%s)", String.join(", ", updatedArguments));
                                holder.registerProblem(reference, messageHarden, new EscapeAllQuotesFix(replacement));
                            }
                        }
                    } else if (functions.contains(functionName)) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2 && arguments[0] instanceof StringLiteralExpression) {
                            final String callback = ((StringLiteralExpression) arguments[0]).getContents();
                            if (callback.equals("htmlentities")) {
                                holder.registerProblem(arguments[0], messageCallback);
                            }
                        }
                    }
                }
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