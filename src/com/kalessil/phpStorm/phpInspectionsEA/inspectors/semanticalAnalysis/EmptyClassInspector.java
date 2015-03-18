package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class EmptyClassInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Class does not contain any fields or methods";

    @NotNull
    public String getShortName() {
        return "EmptyClassInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                if (null != clazz.getNameIdentifier()) {
                    final boolean isEmpty = ((clazz.getOwnFields().length + clazz.getOwnMethods().length) == 0);
                    if (isEmpty) {
                        holder.registerProblem( clazz.getNameIdentifier(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}
