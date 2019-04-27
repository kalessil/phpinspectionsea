package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies.McryptRsaOraclePaddingStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies.OpensslRsaOraclePaddingStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class RsaOraclePaddingAttacksInspector extends LocalInspectionTool {
    @NotNull
    public String getShortName() {
        return "RsaOraclePaddingAttacksInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final List<BooleanSupplier> callbacks = new ArrayList<>(2);
                callbacks.add(() -> OpensslRsaOraclePaddingStrategy.apply(holder, reference));
                callbacks.add(() -> McryptRsaOraclePaddingStrategy.apply(holder, reference));

                for (final BooleanSupplier callback : callbacks) {
                    if (callback.getAsBoolean()) {
                        break;
                    }
                }
                callbacks.clear();
            }
        };
    }
}
