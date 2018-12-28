package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.MultiassignmentExpression;
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

public class MultiAssignmentsUsageInspector extends BasePhpInspection {
    private static final String message = "Using dedicated assignment would be more reliable (e.g '$... = $... + 10' can be mistyped as `$... = $... = 10`).";

    @NotNull
    public String getShortName() {
        return "MultiAssignmentsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression expression) {
                if (!(expression.getParent() instanceof MultiassignmentExpression)) {
                    holder.registerProblem(expression, message);
                }
            }
        };
    }
}
