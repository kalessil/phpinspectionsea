package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Include;
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

public class UsingInclusionReturnValueInspector  extends PhpInspection {
    private static final String message
            = "Operating on this return mechanism is considered a bad practice. OOP can be used instead.";

    @NotNull
    public String getShortName() {
        return "UsingInclusionReturnValueInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                if (this.shouldSkipAnalysis(include, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                if (!OpenapiTypesUtil.isStatementImpl(include.getParent())) {
                    holder.registerProblem(include, message);
                }
            }
        };
    }
}
