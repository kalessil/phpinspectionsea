package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousReturnInspector extends BasePhpInspection {
    private static final String message = "Voids all return and throw statements from the try-block (returned values and exceptions are lost)";

    @NotNull
    public String getShortName() {
        return "SuspiciousReturnInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn statement) {
                PsiElement parent = statement.getParent();
                while (parent != null && !(parent instanceof Finally)) {
                    if (parent instanceof Function || parent instanceof PsiFile) {
                        return;
                    }
                    parent = parent.getParent();
                }

                if (parent != null) {
                    final PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof Try) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(grandParent);
                        if (body != null && PsiTreeUtil.findChildOfAnyType(body, PhpReturn.class, PhpThrow.class) != null) {
                            holder.registerProblem(statement, message);
                        }
                    }
                }
            }
        };
    }
}
