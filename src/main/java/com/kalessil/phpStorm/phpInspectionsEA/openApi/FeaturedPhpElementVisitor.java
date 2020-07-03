package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.intellij.psi.PsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
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

public abstract class FeaturedPhpElementVisitor extends GenericPhpElementVisitor {
    @Override
    protected boolean shouldSkipAnalysis(@NotNull PsiElement target, @NotNull StrictnessCategory category) {
        return ! EAUltimateApplicationComponent.areFeaturesEnabled() || super.shouldSkipAnalysis(target, category);
    }
}
