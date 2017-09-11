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
    private static final String messagePattern = "'scandir(...)' sorts results by default, please specify the second argument.";

    @NotNull
    public String getShortName() {
        return "ScandirUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (functionName != null && arguments.length == 1 && functionName.equals("scandir")) {
                    final String replacement = "scandir(%a%, SCANDIR_SORT_NONE)".replace("%a%", arguments[0].getText());
                    holder.registerProblem(reference, messagePattern, new NoSortFix(replacement));
                }
            }
        };
    }

    private class NoSortFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Disable sorting by default";
        }

        NoSortFix(@NotNull String expression) {
            super(expression);
        }
    }
}
