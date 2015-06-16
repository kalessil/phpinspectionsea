package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpCase;
import com.jetbrains.php.lang.psi.elements.PhpSwitch;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class DegradedSwitchInspector extends BasePhpInspection {
    private static final String strProblemDegraded = "Switch construct behaves as if-else, consider refactoring";
    private static final String strProblemOnlyDefault= "Switch construct has default case only, consider leaving only default's body";

    @NotNull
    public String getShortName() {
        return "DegradedSwitchInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpSwitch(PhpSwitch switchStatement) {
                if (null == switchStatement.getDefaultCase()) {
                    return;
                }

                PhpCase[] cases = switchStatement.getCases();
                if (0 == cases.length) {
                    holder.registerProblem(switchStatement.getFirstChild(), strProblemOnlyDefault, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                if (1 == cases.length) {
                    holder.registerProblem(switchStatement.getFirstChild(), strProblemDegraded, ProblemHighlightType.WEAK_WARNING);
                }
            }
       };
    }
}