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

public class LongInheritanceChainInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Class has 3 or more parent classes, consider using appropriate design patterns.";

    @NotNull
    public String getShortName() {
        return "LongInheritanceChainInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                String strClassFQN = clazz.getFQN();
                /** skip un-explorable and test classes */
                if (
                    StringUtil.isEmpty(strClassFQN) ||
                    strClassFQN.contains("\\Tests\\") || strClassFQN.contains("\\Test\\") ||
                    strClassFQN.endsWith("Test") ||
                    strClassFQN.endsWith("Exception")
                ) {
                    return;
                }

                PsiElement psiClassName = clazz.getNameIdentifier();
                if (null != psiClassName) {
                    int intParentsCount = 0;

                    /** count parents */
                    PhpClass classToCheck = clazz;
                    while (null != classToCheck.getSuperClass()) {
                        classToCheck = classToCheck.getSuperClass();
                        ++intParentsCount;
                    }

                    if (intParentsCount >= 3) {
                        holder.registerProblem(psiClassName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}