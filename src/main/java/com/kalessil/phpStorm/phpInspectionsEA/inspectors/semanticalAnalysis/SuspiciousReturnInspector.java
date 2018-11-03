package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
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
    private static final String messageFinally = "Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).";
    private static final String messageYield   = "It was probably intended to use yield here (currently the returned values is getting ignored).";

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
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(statement))              { return; }

                PsiElement parent = statement.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof Finally) {
                        this.analyzeReturnFromFinally(statement, (Try) parent.getParent());
                        return;
                    }
                    if (parent instanceof Function) {
                        this.analyzeReturnFromGenerator(statement, (Function) parent);
                        return;
                    }
                    parent = parent.getParent();
                }
            }

            private void analyzeReturnFromFinally(@NotNull PhpReturn statement, @NotNull Try scope) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    holder.registerProblem(statement, messageFinally);
                }
            }

            private void analyzeReturnFromGenerator(@NotNull PhpReturn statement, @NotNull Function scope) {
                if (ExpressionSemanticUtil.getReturnValue(statement) != null) {
                    final PhpType type = scope.getType();
                    if (type.filterUnknown().getTypes().stream().anyMatch(t -> t.equals("\\Generator"))) {
                        holder.registerProblem(statement, messageYield);
                    }
                }
            }
        };
    }
}
