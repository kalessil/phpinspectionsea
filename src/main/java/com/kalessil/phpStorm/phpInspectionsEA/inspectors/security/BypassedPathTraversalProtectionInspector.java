package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BypassedPathTraversalProtectionInspector extends LocalInspectionTool {
    private static final String messageFilterVar = "The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.";

    @NotNull
    @Override
    public String getShortName() {
        return "BypassedPathTraversalProtectionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("str_replace")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 3) {
                        final Set<String> second = this.collectPossibleValues(arguments[1]);
                        if (!second.isEmpty() && second.contains("")) {
                            final boolean withQuickFix = !(arguments[0] instanceof ArrayCreationExpression);
                            final Set<String> first    = this.collectPossibleValues(arguments[0]);
                            if (!first.isEmpty() && (first.contains("../") || first.contains("..\\\\"))) {
                                final String replacement = String.format(
                                        "preg_replace('/\\.+[\\/\\\\]+/', '', %s)",
                                        arguments[2].getText()
                                );
                                holder.registerProblem(
                                        reference,
                                        messageFilterVar,
                                        withQuickFix ? new UsePregReplaceFix(replacement) : null
                                );
                            }
                            first.clear();
                        }
                        second.clear();
                    }
                }
            }

            @NotNull
            private Set<String> collectPossibleValues(@NotNull PsiElement argument) {
                final Set<String> result = new HashSet<>();
                if (argument instanceof ArrayCreationExpression) {
                    Arrays.stream(argument.getChildren())
                            .filter(child  -> !(child instanceof ArrayHashElement))
                            .forEach(child -> result.addAll(ExpressionSemanticUtil.resolveAsString(child.getFirstChild())));
                } else {
                    result.addAll(ExpressionSemanticUtil.resolveAsString(argument));
                }
                return result;
            }
        };
    }

    private static final class UsePregReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Harden with preg_replace(...)";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UsePregReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
