package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Include;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class UsingInclusionReturnValueInspector  extends BasePhpInspection {
    private static final String message = "Operating on this return mechanism is considered a bad practice. OOP can be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "UsingInclusionReturnValueInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Usage of inclusion return value";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                if (!OpenapiTypesUtil.isStatementImpl(include.getParent())) {
                    holder.registerProblem(
                            include,
                            MessagesPresentationUtil.prefixWithEa(message)
                    );
                }
            }
        };
    }
}
