package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.strategy.QueryUsageStrategy;
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

public class PdoApiUsageInspector extends BasePhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "PdoApiUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PDO API usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                QueryUsageStrategy.apply(reference, holder);
            }
        };
    }
}

