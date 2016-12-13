package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

public class LongInheritanceChainInspector extends BasePhpInspection {
    private static final String message = "Class has 3 or more parent classes, consider using appropriate design patterns.";

    @NotNull
    public String getShortName() {
        return "LongInheritanceChainInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final PsiElement psiClassName = NamedElementUtil.getNameIdentifier(clazz);
                final String className        = clazz.getName();
                /* skip un-reportable, exception and test classes */
                if (null == psiClassName || FileSystemUtil.isTestClass(clazz) || className.endsWith("Exception")) {
                    return;
                }

                /* count parents */
                int intParentsCount   = 0;
                PhpClass classToCheck = clazz;
                /* in source code class CAN extend itself, PS will report it but data structure is incorrect still */
                while (null != classToCheck.getSuperClass() && clazz != classToCheck.getSuperClass()) {
                    classToCheck = classToCheck.getSuperClass();
                    ++intParentsCount;
                }

                if (intParentsCount >= 3 && !clazz.isDeprecated()) {
                    holder.registerProblem(psiClassName, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}