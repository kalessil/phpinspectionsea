package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
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
                /* require class with name which can be targeted by warning */
                final String className    = clazz.getName();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                if (null == nameNode || clazz.isInterface() || clazz.isTrait() || className.endsWith("Exception")) {
                    return;
                }

                /* check if class is empty, take into account used traits and deprecation */
                final boolean isEmpty = (0 == clazz.getOwnFields().length + clazz.getOwnMethods().length);
                if (isEmpty && !clazz.isDeprecated() && 0 == clazz.getTraits().length) {
                    /* false-positive: inheriting abstract classes */
                    final PhpClass parentClass = clazz.getSuperClass();
                    if (null != parentClass && parentClass.isAbstract()) {
                        return;
                    }

                    holder.registerProblem(nameNode, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
