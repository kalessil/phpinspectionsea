package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
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

public class SuspiciousSemicolonInspector extends BasePhpInspection {
    private static final String message = "Probably a bug, because ';' treated as body";

    @NotNull
    public String getShortName() {
        return "SuspiciousSemicolonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStatement(Statement statement) {
                if (0 == statement.getChildren().length) {
                    final PsiElement parent = statement.getParent();
                    if (null == parent) {
                        return;
                    }

                    if (
                        parent instanceof DoWhile || parent instanceof While || parent instanceof For || parent instanceof ForeachStatement ||
                        parent instanceof If || parent instanceof ElseIf || parent instanceof Else
                    ) {
                        holder.registerProblem(statement, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}

