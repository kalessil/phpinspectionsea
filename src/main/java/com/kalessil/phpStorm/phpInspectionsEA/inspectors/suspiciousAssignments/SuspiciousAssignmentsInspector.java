package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousAssignmentsInspector extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousAssignmentsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpSwitch(@NotNull PhpSwitch switchStatement) {
                if (this.shouldSkipAnalysis(switchStatement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                SwitchFallThroughStrategy.apply(switchStatement, holder);
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                SelfAssignmentStrategy.apply(expression, holder);
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (!this.isTestContext(method)) {
                    ParameterImmediateOverrideStrategy.apply(method, holder);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (!this.isTestContext(function)) {
                    ParameterImmediateOverrideStrategy.apply(function, holder);
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignment) {
                if (this.shouldSkipAnalysis(assignment, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                /* because this hook fired e.g. for `.=` assignments (a BC break by PhpStorm) */
                if (OpenapiTypesUtil.isAssignment(assignment)) {
                    SuspiciousOperatorFormattingStrategy.apply(assignment, holder);
                    SequentialAssignmentsStrategy.apply(assignment, holder);
                }
            }
        };
    }
}
