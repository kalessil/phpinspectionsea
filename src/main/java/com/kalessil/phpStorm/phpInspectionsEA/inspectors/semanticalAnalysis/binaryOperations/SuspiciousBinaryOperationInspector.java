package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousBinaryOperationInspector extends BasePhpInspection {
    // Inspection options.
    public boolean VERIFY_CONSTANTS_IN_CONDITIONS       = true;
    public boolean VERIFY_UNCLEAR_OPERATIONS_PRIORITIES = true;

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousBinaryOperationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious binary operations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final Collection<BooleanSupplier> callbacks = new ArrayList<>();
                callbacks.add(() -> InstanceOfTraitStrategy.apply(expression, holder));
                callbacks.add(() -> EqualsInAssignmentContextStrategy.apply(expression, holder));
                callbacks.add(() -> GreaterOrEqualInHashElementStrategy.apply(expression, holder));
                callbacks.add(() -> NullableArgumentComparisonStrategy.apply(expression, holder));
                callbacks.add(() -> IdenticalOperandsStrategy.apply(expression, holder));
                callbacks.add(() -> MisplacedOperatorStrategy.apply(expression, holder));
                callbacks.add(() -> NullCoalescingOperatorCorrectnessStrategy.apply(expression, holder));
                callbacks.add(() -> ConcatenationWithArrayStrategy.apply(expression, holder));
                if (VERIFY_CONSTANTS_IN_CONDITIONS) {
                    callbacks.add(() -> HardcodedConstantValuesStrategy.apply(expression, holder));
                }
                if (VERIFY_UNCLEAR_OPERATIONS_PRIORITIES) {
                    callbacks.add(() -> UnclearOperationsPriorityStrategy.apply(expression, holder));
                }

                /* run through strategies until the first one fired something */
                for (final BooleanSupplier strategy: callbacks) {
                    if (strategy.getAsBoolean()) {
                        break;
                    }
                }
                callbacks.clear();
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Verify operations priorities", VERIFY_UNCLEAR_OPERATIONS_PRIORITIES, (isSelected) -> VERIFY_UNCLEAR_OPERATIONS_PRIORITIES = isSelected);
            component.addCheckbox("Verify enforced conditions (with e.g. true)", VERIFY_CONSTANTS_IN_CONDITIONS, (isSelected) -> VERIFY_CONSTANTS_IN_CONDITIONS = isSelected);
        });
    }
}
