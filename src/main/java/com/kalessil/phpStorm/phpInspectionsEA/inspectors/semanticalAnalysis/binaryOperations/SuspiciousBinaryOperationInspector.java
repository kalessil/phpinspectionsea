package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    public String getShortName() {
        return "SuspiciousBinaryOperationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final Collection<BooleanSupplier> callbacks = new ArrayList<>(11);
                callbacks.add(() -> InstanceOfTraitStrategy.apply(expression, holder));
                callbacks.add(() -> EqualsInAssignmentContextStrategy.apply(expression, holder));
                callbacks.add(() -> GreaterOrEqualInHashElementStrategy.apply(expression, holder));
                callbacks.add(() -> NullableArgumentComparisonStrategy.apply(expression, holder));
                callbacks.add(() -> IdenticalOperandsStrategy.apply(expression, holder));
                callbacks.add(() -> MisplacedOperatorStrategy.apply(expression, holder));
                callbacks.add(() -> MistypedLogicalOperatorsStrategy.apply(expression, holder));
                callbacks.add(() -> NullCoalescingOperatorCorrectnessStrategy.apply(expression, holder));
                callbacks.add(() -> HardcodedConstantValuesStrategy.apply(expression, holder));
                callbacks.add(() -> UnclearOperationsPriorityStrategy.apply(expression, holder));
                callbacks.add(() -> MultipleValuesEqualityStrategy.apply(expression, holder));

                /* TODO: === and !== on non-intersecting types */

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
}
