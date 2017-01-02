package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class EmptyClassInspector extends BasePhpInspection {
    private static final String message = "Class does not contain any properties or methods.";

    @NotNull
    public String getShortName() {
        return "EmptyClassInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final String className = clazz.getName();
                /* skip un-explorable and exception classes */
                if (StringUtil.isEmpty(className) || className.endsWith("Exception")) {
                    return;
                }

                /* require class with name which can be targeted by warning */
                final PsiElement psiClassName = clazz.getNameIdentifier();
                if (null == psiClassName || clazz.isInterface() || clazz.isTrait()) {
                    return;
                }

                /* check if class is empty, take into account used traits */
                final boolean isEmpty = (0 == clazz.getOwnFields().length + clazz.getOwnMethods().length);
                if (isEmpty && !clazz.isDeprecated() && 0 == clazz.getTraits().length) {
                    holder.registerProblem(psiClassName, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
