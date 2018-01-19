package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
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

public class MissingElseKeywordInspector extends BasePhpInspection {
    private static final String message = "It's probably was intended to use 'else if' or 'elseif' here.";

    @NotNull
    public String getShortName() {
        return "MissingElseKeywordInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If expression) {
                final PsiElement previous = expression.getPrevSibling();
                if (previous instanceof PsiWhiteSpace && previous.getText().equals(" ")) {
                    final PsiElement candidate = previous.getPrevSibling();
                    if (candidate instanceof If) {
                        PsiElement last = candidate;
                        while (last != null && !(last instanceof GroupStatement)) {
                            last = last.getLastChild();
                        }
                        if (last != null && !(last.getParent() instanceof Else)) {
                            holder.registerProblem(expression.getFirstChild(), message);
                        }
                    }
                }
            }
        };
    }
}
