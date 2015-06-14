package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SwitchContinuationInLoopInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This behaves differently in PHP: use 'continue 2;' for continuation of an externals loop";

    @NotNull
    public String getShortName() {
        return "SwitchContinuationInLoopInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpContinue(PhpContinue continueStatement) {
                /* check if continue already defined with desired level */
                if (null != continueStatement.getFirstPsiChild()) {
                    return;
                }

                boolean isSwitch = false;
                PsiElement objParent = continueStatement.getParent();
                while (null != objParent) {
                    /* reached file or callable */
                    if (objParent instanceof PhpFile || objParent instanceof Function) {
                        return;
                    }

                    /* check if shall operate on loop-switch-continue analysis */
                    if (!isSwitch && objParent instanceof PhpSwitch) {
                        isSwitch = true;
                    }

                    /* when met a loop, complete analysis */
                    if (objParent instanceof For || objParent instanceof ForeachStatement || objParent instanceof While) {
                        if (isSwitch) {
                            holder.registerProblem(continueStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                        }

                        return;
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }
}