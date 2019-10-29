package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class SuspiciousBinaryOperationInspector extends PhpInspection {
    // Inspection options.
    public boolean VERIFY_CONSTANTS_VALUES_CHECKS       = true;
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
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final Collection<BooleanSupplier> callbacks = new ArrayList<>(20);
                /* pit-falls and all sort of weak-typed language issues*/
                callbacks.add(() -> PossiblyAssignmentStrategy.apply(expression, holder));
                callbacks.add(() -> PossiblyArrayHashElementDeclarationStrategy.apply(expression, holder));
                callbacks.add(() -> IdenticalOperandsStrategy.apply(expression, holder));
                callbacks.add(() -> InvalidArrayOperationStrategy.apply(expression, holder));
                callbacks.add(() -> InstanceOfTraitStrategy.apply(expression, holder));
                callbacks.add(() -> PossiblyMethodReferenceStrategy.apply(expression, holder));
                callbacks.add(() -> NotOperatorLackingTypeCheckStrategy.apply(expression, holder));
                callbacks.add(() -> MisplacedOperatorStrategy.apply(expression, holder));
                callbacks.add(() -> MistypedLogicalOperatorsStrategy.apply(expression, holder));
                callbacks.add(() -> NullCoalescingOperatorCorrectnessStrategy.apply(expression, holder));
                /* constant conditions */
                callbacks.add(() -> MultipleFalsyValuesCheckStrategy.apply(expression, holder));
                callbacks.add(() -> SimplifyBooleansComparisonStrategy.apply(expression, holder));
                callbacks.add(() -> MultipleValuesEqualityInBinaryStrategy.apply(expression, holder));
                callbacks.add(() -> MultipleValuesEqualityInIfBodyStrategy.apply(expression, holder));
                if (VERIFY_CONSTANTS_VALUES_CHECKS) {
                    callbacks.add(() -> ConstantConditionsConstantsValuesStrategy.apply(expression, holder));
                    callbacks.add(() -> ConstantConditionsPhpVersionStrategy.apply(expression, holder));
                }
                callbacks.add(() -> ConstantConditionsCountCheckStrategy.apply(expression, holder));
                callbacks.add(() -> TypesIntersectionStrategy.apply(expression, holder));
                /* generic cases */
                if (VERIFY_CONSTANTS_IN_CONDITIONS) {
                    callbacks.add(() -> HardcodedConstantValuesStrategy.apply(expression, holder));
                }
                if (VERIFY_UNCLEAR_OPERATIONS_PRIORITIES) {
                    callbacks.add(() -> UnclearOperationsPriorityStrategy.apply(expression, holder));
                }

                /* run through strategies until the first one fired a warning */
                for (final BooleanSupplier strategy: callbacks) {
                    if (strategy.getAsBoolean()) {
                        break;
                    }
                }

                callbacks.clear();
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                ConstantConditionsIsNumericStrategy.apply(reference, holder);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Verify operations priorities", VERIFY_UNCLEAR_OPERATIONS_PRIORITIES, (isSelected) -> VERIFY_UNCLEAR_OPERATIONS_PRIORITIES = isSelected);
            component.addCheckbox("Verify constants values checks", VERIFY_CONSTANTS_VALUES_CHECKS, (isSelected) -> VERIFY_CONSTANTS_VALUES_CHECKS = isSelected);
            component.addCheckbox("Verify enforced conditions (with e.g. true)", VERIFY_CONSTANTS_IN_CONDITIONS, (isSelected) -> VERIFY_CONSTANTS_IN_CONDITIONS = isSelected);
        });
    }
}
