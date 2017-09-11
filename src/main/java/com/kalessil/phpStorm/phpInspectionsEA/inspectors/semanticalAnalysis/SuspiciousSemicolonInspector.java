package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.ElseIf;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class SuspiciousSemicolonInspector extends BasePhpInspection {
    private static final String message = "Probably a bug, because ';' treated as body.";

    @NotNull
    public String getShortName() {
        return "SuspiciousSemicolonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpStatement(@NotNull Statement statement) {
                if (statement.getChildren().length == 0) {
                    final PsiElement parent = statement.getParent();
                    if (parent != null) {
                        if (
                            OpenapiTypesUtil.isLoop(parent) ||
                            parent instanceof If || parent instanceof ElseIf || parent instanceof Else
                        ) {
                            holder.registerProblem(statement, message);
                        }
                    }
                }
            }
        };
    }
}

