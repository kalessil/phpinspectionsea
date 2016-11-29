package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SequentialUnSetCallsInspector extends BasePhpInspection {
    private static final String message = "Can be safely replaced with 'unset(..., ...[, ...])' construction";

    @NotNull
    public String getShortName() {
        return "SequentialUnSetCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnset(PhpUnset unsetStatement) {
                PsiElement previous = unsetStatement.getPrevPsiSibling();
                while (previous instanceof PhpDocComment) {
                    previous = ((PhpDocComment) previous).getPrevPsiSibling();
                }

                if (previous instanceof PhpUnset) {
                    holder.registerProblem(unsetStatement, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}