package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SequentialUnSetCallsInspector extends BasePhpInspection {
    private static final String strProblemDescription =
            "This call is ambiguous: unset accepts multiple arguments";

    @NotNull
    public String getDisplayName() {
        return "Performance: 'unset(...)' calls can be merged";
    }

    @NotNull
    public String getShortName() {
        return "SequentialUnSetCallsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnset(PhpUnset unsetStatement) {
                if (!(unsetStatement.getPrevPsiSibling() instanceof PhpUnset)) {
                    return;
                }

                holder.registerProblem(unsetStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}