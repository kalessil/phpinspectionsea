package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LowPerformingDirectoryOperationsInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s(...)' sorts results by default, please provide second argument for specifying the intention.";

    @NotNull
    public String getShortName() {
        return "LowPerformingDirectoryOperationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("scandir") || functionName.equals("glob"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && this.isFromRootNamespace(reference)) {
                        final String replacement = String.format(
                                "%s%s(%s, %s)",
                                reference.getImmediateNamespaceName(),
                                functionName,
                                arguments[0].getText(),
                                functionName.equals("scandir") ? "SCANDIR_SORT_NONE" : "GLOB_NOSORT"
                        );
                        holder.registerProblem(reference, String.format(messagePattern, functionName), new NoSortFix(replacement));
                    }
                }
            }
        };
    }

    private static final class NoSortFix extends UseSuggestedReplacementFixer {
        private static final String title = "Disable sorting by default";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        NoSortFix(@NotNull String expression) {
            super(expression);
        }
    }
}
