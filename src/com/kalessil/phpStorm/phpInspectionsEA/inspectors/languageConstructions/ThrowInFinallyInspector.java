package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Finally;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpThrow;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ThrowInFinallyInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "Exceptions handling inside finally has variety of side-effects in certain PHP versions (especially in 5.5).";

    @NotNull
    public String getShortName() {
        return "ThrowInFinallyInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpThrow(PhpThrow throwStatement) {
                PsiElement objParent = throwStatement.getParent();
                while (null != objParent) {
                    /* reached file or class */
                    if (objParent instanceof PhpFile || objParent instanceof PhpClass) {
                        return;
                    }

                    /* when met a finally, complete analysis */
                    if (objParent instanceof Finally) {
                        holder.registerProblem(throwStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                        return;
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }
}

