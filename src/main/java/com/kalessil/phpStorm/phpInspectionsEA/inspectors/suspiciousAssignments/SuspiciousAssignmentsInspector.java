package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy.*;
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

public class SuspiciousAssignmentsInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "SuspiciousAssignmentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpSwitch(@NotNull PhpSwitch switchStatement) {
                SwitchFallThroughStrategy.apply(switchStatement, holder);
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                SelfAssignmentStrategy.apply(expression, holder);
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                ParameterImmediateOverrideStrategy.apply(method, holder);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                ParameterImmediateOverrideStrategy.apply(function, holder);
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                SuspiciousOperatorFormattingStrategy.apply(assignmentExpression, holder);
                SequentialAssignmentsStrategy.apply(assignmentExpression, holder);
            }
        };
    }
}
