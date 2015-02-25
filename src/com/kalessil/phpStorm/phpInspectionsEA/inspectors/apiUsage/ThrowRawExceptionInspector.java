package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpThrow;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ThrowRawExceptionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "One of SPL exceptions shall be thrown - \\Exception is too general";

    @NotNull
    public String getShortName() {
        return ...;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpThrow(PhpThrow throwStatement) {
                if (throwStatement.getArgument() instanceof NewExpression) {
                    ClassReference classReference = ((NewExpression) throwStatement.getArgument()).getClassReference();
                    if (null != classReference && null != classReference.getFQN() && classReference.getFQN().equals("\\Exception")) {
                        holder.registerProblem(throwStatement, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}
