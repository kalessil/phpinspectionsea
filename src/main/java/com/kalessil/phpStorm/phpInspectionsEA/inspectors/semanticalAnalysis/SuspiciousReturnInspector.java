package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
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
            public void visitPhpReturn(PhpReturn returnStatement) {
                if (this.isContainingFileSkipped(returnStatement)) { return; }

                PsiElement parent = returnStatement.getParent();
                while (null != parent) {
                    if (parent instanceof Function || parent instanceof PsiFile) {
                        return;
                    }
                    if (parent instanceof Finally) {
                        break;
                    }

                    parent = parent.getParent();
                }

                if (null != parent && parent.getParent() instanceof Try) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(parent.getParent());
                    if (null != body) {
                        final PsiElement firstReturn = PsiTreeUtil.findChildOfAnyType(body, PhpReturn.class, PhpThrow.class);
                        if (null != firstReturn) {
                            holder.registerProblem(returnStatement, message, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }

            // TODO: $x = 0 == '';
            // TODO: if-return-return, when returned values are the same
        };
    }
}
