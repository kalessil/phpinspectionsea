package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy.ChainedCallsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy.NullableVariablesStrategy;
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

public class NullPointerExceptionInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "NullPointerExceptionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (!method.isAbstract()) {
                    NullableVariablesStrategy.applyToParameters(method, holder);
                    ChainedCallsStrategy.apply(method, holder);
                    NullableVariablesStrategy.applyToVariables(method, holder);
                }
            }
        };
    }
}
