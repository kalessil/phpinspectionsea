package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy.PropertyUsedInPrivateContextStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy.ProtectedMembersOfFinalClassStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LowerAccessLevelInspector extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "LowerAccessLevelInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Declaration access can be weaker";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpField(@NotNull Field field) {
                if (this.shouldSkipAnalysis(field, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                ProtectedMembersOfFinalClassStrategy.apply(field, problemsHolder);
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                ProtectedMembersOfFinalClassStrategy.apply(method, problemsHolder);
            }

            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                PropertyUsedInPrivateContextStrategy.apply(clazz, problemsHolder);
            }
        };
    }
}
