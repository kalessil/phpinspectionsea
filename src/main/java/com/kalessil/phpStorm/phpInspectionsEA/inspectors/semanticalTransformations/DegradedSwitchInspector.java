package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpCase;
import com.jetbrains.php.lang.psi.elements.PhpSwitch;
import com.jetbrains.php.lang.inspections.PhpInspection;
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

public class DegradedSwitchInspector extends PhpInspection {
    private static final String messageIf          = "Switch construct behaves as if, consider refactoring.";
    private static final String messageIfElse      = "Switch construct behaves as if-else, consider refactoring.";
    private static final String messageOnlyDefault = "Switch construct has default case only, consider leaving only the default case's body.";

    @NotNull
    public String getShortName() {
        return "DegradedSwitchInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpSwitch(@NotNull PhpSwitch expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PhpCase[] cases = expression.getCases();
                if (cases.length == 0) {
                    if (expression.getDefaultCase() != null) {
                        holder.registerProblem(expression.getFirstChild(), messageOnlyDefault);
                    }
                } else if (cases.length == 1) {
                    if (expression.getDefaultCase() == null) {
                        holder.registerProblem(expression.getFirstChild(), messageIf);
                    } else {
                        holder.registerProblem(expression.getFirstChild(), messageIfElse);
                    }
                }
            }
       };
    }
}