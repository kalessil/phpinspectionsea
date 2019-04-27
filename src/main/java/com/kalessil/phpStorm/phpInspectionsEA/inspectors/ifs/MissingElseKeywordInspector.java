package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
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

public class MissingElseKeywordInspector extends PhpInspection {
    private static final String message = "It's probably was intended to use 'else if' or 'elseif' here.";

    @NotNull
    public String getShortName() {
        return "MissingElseKeywordInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                /* get through previous space to if-statement */
                PsiElement previous = expression.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    final String spacing = previous.getText();
                    if (spacing.isEmpty() || spacing.equals(" ")) {
                        previous = previous.getPrevSibling();
                    }
                }
                /* analyze previous statement */
                if (previous instanceof If) {
                    PsiElement last = previous;
                    while (last != null && !(last instanceof GroupStatement)) {
                        last = last.getLastChild();
                    }
                    if (last != null && !(last.getParent() instanceof Else)) {
                        holder.registerProblem(expression.getFirstChild(), message);
                    }
                }
            }
        };
    }
}
