package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousReturnInspector extends PhpInspection {
    private static final String messageFinally = "Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).";
    private static final String messageYield   = "It was probably intended to use 'yield' or 'yield from' here.";

    final private static Condition<PsiElement> PARENT_FUNCTION = new Condition<PsiElement>() {
        public boolean value(PsiElement element) { return element instanceof Function; }
        public String toString()                 { return "Condition.PARENT_FUNCTION"; }
    };

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousReturnInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious returns";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

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
                    holder.registerProblem(
                            statement,
                            MessagesPresentationUtil.prefixWithEa(messageFinally)
                    );
                }
            }

            private void analyzeReturnFromGenerator(@NotNull PhpReturn statement, @NotNull Function scope) {
                if (ExpressionSemanticUtil.getReturnValue(statement) != null) {
                    final PhpType type = scope.getType();
                    if (type.filterUnknown().getTypes().stream().anyMatch(t -> t.equals("\\Generator"))) {
                        final boolean hasYields = PsiTreeUtil.findChildrenOfType(scope, PhpYield.class).stream()
                                .anyMatch(yield -> PsiTreeUtil.findFirstParent(yield, PARENT_FUNCTION) == scope);
                        if (hasYields) {
                            holder.registerProblem(
                                    statement,
                                    MessagesPresentationUtil.prefixWithEa(messageYield)
                            );
                        }
                    }
                }
            }
        };
    }
}
