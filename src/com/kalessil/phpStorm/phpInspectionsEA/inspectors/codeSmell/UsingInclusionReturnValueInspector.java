package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Include;
import com.jetbrains.php.lang.psi.elements.impl.ControlStatementImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UsingInclusionReturnValueInspector  extends BasePhpInspection {
    private static final String message = "Operating on this return mechanism considered a bad practice. OOP can be used instead.";

    @NotNull
    public String getShortName() {
        return "UsingInclusionReturnValueInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpInclude(Include include) {
                final PsiElement parent = include.getParent();
                if (parent instanceof ControlStatementImpl || !(parent instanceof StatementImpl)) {
                    holder.registerProblem(include, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
