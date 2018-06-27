package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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

public class ScandirUsageInspector extends BasePhpInspection {
    private static final String message = "'scandir(...)' sorts results by default, please specify the second argument.";

    @NotNull
    public String getShortName() {
        return "ScandirUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* note: glob is also sorting by default, but it lacking sort-ing flag, hence glob inspection skipped */

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("scandir")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && this.isFromRootNamespace(reference)) {
                        final String replacement = String.format("scandir(%s, SCANDIR_SORT_NONE)", arguments[0].getText());
                        holder.registerProblem(reference, message, new NoSortFix(replacement));
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
