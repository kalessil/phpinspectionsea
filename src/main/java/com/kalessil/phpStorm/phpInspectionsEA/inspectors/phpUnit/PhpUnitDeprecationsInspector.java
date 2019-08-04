package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PhpUnitDeprecationsInspector extends PhpInspection {
    private final static String messageDeprecated = "%s is deprecated in favor of %s() since PhpUnit 8.";
    private final static String messageRemoved    = "%s is deprecated since PhpUnit 8.";

    @NotNull
    @Override
    public String getShortName() {
        return "PhpUnitDeprecationsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PhpUnit: API deprecations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PHPUNIT)) { return; }

                final String methodName = reference.getName();
                if (methodName != null && (methodName.equals("assertEquals") || methodName.equals("assertNotEquals"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 3) {
                        if (arguments.length >= 4 && !arguments[3].getText().isEmpty()) {
                            final String message = String.format(messageDeprecated, "$delta", methodName + "WithDelta");
                            holder.registerProblem(arguments[3], message, ProblemHighlightType.LIKE_DEPRECATED);
                        }
                        if (arguments.length >= 5 && !arguments[4].getText().isEmpty()) {
                            final String message = String.format(messageRemoved, "$maxDepth");
                            holder.registerProblem(arguments[4], message, ProblemHighlightType.LIKE_DEPRECATED);
                        }
                        if (arguments.length >= 6 && !arguments[5].getText().isEmpty()) {
                            final String message = String.format(messageDeprecated, "$canonicalize", methodName + "Canonicalizing");
                            holder.registerProblem(arguments[5], message, ProblemHighlightType.LIKE_DEPRECATED);
                        }
                        if (arguments.length >= 7 && !arguments[6].getText().isEmpty()) {
                            final String message = String.format(messageDeprecated, "$ignoreCase", methodName + "IgnoringCase");
                            holder.registerProblem(arguments[6], message, ProblemHighlightType.LIKE_DEPRECATED);
                        }
                    }
                }
            }
        };
    }
}
