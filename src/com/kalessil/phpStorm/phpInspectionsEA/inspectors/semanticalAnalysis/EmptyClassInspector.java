package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class EmptyClassInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Class does not contain any properties or methods";

    @NotNull
    public String getShortName() {
        return "EmptyClassInspection";
    }

    @Override
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

                /* require class with name which can be targeted by warning */
                if (! clazz.isInterface() && ! clazz.isTrait() && null != clazz.getNameIdentifier()) {
                    final boolean isEmpty = (0 == (
                            clazz.getOwnFields().length + clazz.getOwnMethods().length + clazz.getTraitUseRules().size()
                        ));
                    if (isEmpty) {
                        holder.registerProblem( clazz.getNameIdentifier(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}
