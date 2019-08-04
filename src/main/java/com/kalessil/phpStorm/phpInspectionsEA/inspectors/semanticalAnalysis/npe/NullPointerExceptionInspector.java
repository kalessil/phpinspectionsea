package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy.ChainedCallsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy.NestedCallsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy.NullableVariablesStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
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

public class NullPointerExceptionInspector extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "NullPointerExceptionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Null reference";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (!method.isAbstract() && !this.isTestContext(method)) {
                    NullableVariablesStrategy.applyToParameters(method, holder);
                    NullableVariablesStrategy.applyToLocalVariables(method, holder);
                    ChainedCallsStrategy.apply(method, holder);
                    NestedCallsStrategy.apply(method, holder);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                NullableVariablesStrategy.applyToParameters(function, holder);
                NullableVariablesStrategy.applyToLocalVariables(function, holder);
                ChainedCallsStrategy.apply(function, holder);
            }
        };
    }
}
