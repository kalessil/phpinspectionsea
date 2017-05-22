package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class MagicNumberInspector extends BasePhpInspection {
    private static final String message = "Magic number should be replaced by a constant.";

    @NotNull
    public String getShortName() {
        return "MagicNumberInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpExpression(final PhpExpression expression) {
                if (PhpType.intersects(expression.getType(), PhpType.FLOAT_INT)) {
                    if (!(expression.getParent() instanceof PhpReturn)) {
                        return;
                    }

                    problemsHolder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
